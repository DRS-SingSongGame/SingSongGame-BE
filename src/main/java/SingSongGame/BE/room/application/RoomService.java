package SingSongGame.BE.room.application;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.room.application.converter.RoomRequestConverter;
import SingSongGame.BE.room.application.converter.RoomResponseConverter;
import SingSongGame.BE.room.application.dto.request.CreateRoomRequest;
import SingSongGame.BE.room.application.dto.request.JoinRoomRequest;
import SingSongGame.BE.room.application.dto.response.CreateRoomResponse;
import SingSongGame.BE.room.application.dto.response.ExitRoomResponse;
import SingSongGame.BE.room.application.dto.response.GetRoomResponse;
import SingSongGame.BE.room.application.dto.response.JoinRoomResponse;
import SingSongGame.BE.room.persistence.GameStatus;
import SingSongGame.BE.room.persistence.Room;
import SingSongGame.BE.room.persistence.RoomRepository;
import SingSongGame.BE.in_game.persistence.InGame;
import SingSongGame.BE.in_game.persistence.InGameRepository;
import SingSongGame.BE.chat.service.RoomChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final InGameRepository inGameRepository;
    private final RoomChatService roomChatService;
    private final RoomRequestConverter requestConverter;
    private final RoomResponseConverter responseConverter;

    @Transactional
    public CreateRoomResponse createRoom(CreateRoomRequest request, User hostUser) {
        Room room = requestConverter.toEntity(request, hostUser);
        Long saveId = roomRepository.save(room).getId();
        return responseConverter.from(saveId);
    }

    // 로비 내부 방들을 조회할 때 비밀번호를 함께 넘겨줘서 프론트한테 각 방의 비밀번호를 가지고 있는게 맞을까?
    // 아니면 유저가 특정 방을 접속할 때 비밀번호를 조회하는게 맞을까?
    public List<GetRoomResponse> getRoomsInRoby() {
        List<Room> rooms = roomRepository.findAll()
                                         .stream()
                                         .filter(type -> type.getGameStatus() != GameStatus.DELETED)
                                         .collect(Collectors.toList());
        List<GetRoomResponse> response = responseConverter.from(rooms);
        return response;
    }

    @Transactional
    public JoinRoomResponse joinRoom(JoinRoomRequest request, User user, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));
        
        // 방 상태 확인
        if (room.getGameStatus() == GameStatus.DELETED) {
            throw new IllegalArgumentException("삭제된 방입니다.");
        }
        
        if (room.getGameStatus() == GameStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("게임이 진행 중인 방입니다.");
        }
        
        // 비밀번호 확인
        if (room.getIsPrivate()) {
            if (room.getPassword().equals(request.getPassword())){
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
        }
        
        // 방 인원 수 확인
        long currentPlayerCount = inGameRepository.countByRoom(room);
        if (currentPlayerCount >= room.getMaxPlayer()) {
            throw new IllegalArgumentException("방이 가득 찼습니다.");
        }
        
        // 이미 방에 있는지 확인
        boolean alreadyInRoom = inGameRepository.existsByRoomAndUser(room, user);
        if (alreadyInRoom) {
            throw new IllegalArgumentException("이미 방에 입장해 있습니다.");
        }
        
        // 방에 입장
        InGame inGame = InGame.builder()
                .room(room)
                .user(user)
                .build();
        inGameRepository.save(inGame);
        
        // 방 입장 채팅 메시지 전송
        roomChatService.sendRoomEnterMessage(user, room.getId());
        
        // 현재 방 인원 수 다시 조회
        currentPlayerCount = inGameRepository.countByRoom(room);
//        if (currentPlayerCount == room.getMaxPlayer()) {
//            room.getGameStatus().name() = String.valueOf(GameStatus.FULL);
//        }
        return JoinRoomResponse.builder()
                .roomId(room.getId())
                .roomName(room.getName())
                .hostName(room.getHost().getName())
                .currentPlayerCount((int) currentPlayerCount)
                .maxPlayer(room.getMaxPlayer())
                .gameStatus(room.getGameStatus().name())
                .build();
    }

    @Transactional
    public ExitRoomResponse leaveRoom(Long roomId, User user) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));
        
        InGame inGame = inGameRepository.findByRoomAndUser(room, user)
                .orElseThrow(() -> new IllegalArgumentException("방에 입장해 있지 않습니다."));
        
        // 방 퇴장 채팅 메시지 전송 (삭제 전에)
        roomChatService.sendRoomLeaveMessage(user, roomId);
        
        inGameRepository.delete(inGame);
        
        // 방장이 나가는 경우 방 삭제
        if (room.getHost().getId().equals(user.getId())) {
            room.updateGameStatus(GameStatus.DELETED);
        }

        Long restNumber = inGameRepository.countByRoom(room);

        List<User> users = inGameRepository.findAllByRoom(room).stream()
                                           .map(x -> x.getUser())
                                           .collect(Collectors.toList());

        return ExitRoomResponse.builder()
                               .currentPlayer(restNumber)
                               .gameStatus(GameStatus.WAITING)
                               .users(users)
                               .build();
    }
}
