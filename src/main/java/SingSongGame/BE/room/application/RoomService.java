package SingSongGame.BE.room.application;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.chat.service.RoomChatService;
import SingSongGame.BE.in_game.persistence.GameSession;
import SingSongGame.BE.in_game.persistence.GameSessionRepository;
import SingSongGame.BE.in_game.persistence.InGame;
import SingSongGame.BE.in_game.persistence.InGameRepository;
import SingSongGame.BE.quick_match.persistence.QuickMatchRepository;
import SingSongGame.BE.quick_match.persistence.QuickMatchRoomPlayerRepository;
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
import SingSongGame.BE.room.persistence.RoomType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final InGameRepository inGameRepository;
    private final RoomChatService roomChatService;
    private final RoomRequestConverter requestConverter;
    private final RoomResponseConverter responseConverter;
    private final GameSessionRepository gameSessionRepository;
    private final QuickMatchRepository quickMatchRepository;
    private final QuickMatchRoomPlayerRepository quickMatchRoomPlayerRepository;

    @Transactional
    public CreateRoomResponse createRoom(CreateRoomRequest request, User hostUser) {
        Room room = requestConverter.toEntity(request, hostUser);
        roomRepository.save(room);
        return responseConverter.from(room);
    }

    public List<GetRoomResponse> getRoomsInRoby() {
        List<Room> rooms = roomRepository.findAll();
        return rooms.stream()
                .filter(room -> {
                    Optional<GameSession> gameSessionOptional = gameSessionRepository.findById(room.getId());
                    // GameSession이 없거나, GameStatus가 DELETED가 아닌 경우만 필터링
                    return gameSessionOptional.map(gameSession -> gameSession.getGameStatus() != GameStatus.DELETED)
                            .orElse(true); // GameSession이 없으면 (게임 진행 중이 아니면) 항상 포함
                })
                .map(room -> {
                    Optional<GameSession> gameSessionOptional = gameSessionRepository.findById(room.getId());
                    GameStatus currentStatus = gameSessionOptional.map(GameSession::getGameStatus).orElse(GameStatus.WAITING); // GameSession이 없으면 WAITING
                    return responseConverter.from(room, currentStatus);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public JoinRoomResponse joinRoom(JoinRoomRequest request, User user, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "존재하지 않는 방입니다."));

        Optional<GameSession> gameSessionOptional = gameSessionRepository.findById(roomId);
        GameStatus currentStatus = gameSessionOptional.map(GameSession::getGameStatus).orElse(GameStatus.WAITING);

        // 방 상태 확인
        if (currentStatus == GameStatus.DELETED) {
            throw new ResponseStatusException(BAD_REQUEST, "삭제된 방입니다.");
        }

        if (currentStatus == GameStatus.IN_PROGRESS) {
            throw new ResponseStatusException(BAD_REQUEST, "게임이 진행 중인 방입니다.");
        }

        // 비밀번호 확인
        if (room.getIsPrivate()) {
            if (!room.getPassword().equals(request.getPassword())) { // 비밀번호 불일치 시 예외 발생
                throw new ResponseStatusException(BAD_REQUEST, "비밀번호가 일치하지 않습니다.");
            }
        }

        // 방 인원 수 확인
        long currentPlayerCount = inGameRepository.countByRoom(room);
        if (currentPlayerCount >= room.getMaxPlayer()) {
            throw new ResponseStatusException(BAD_REQUEST, "방이 가득 찼습니다.");
        }

        // 이미 방에 있는지 확인
        boolean alreadyInRoom = inGameRepository.existsByRoomAndUser(room, user);
        if (alreadyInRoom) {
            throw new ResponseStatusException(BAD_REQUEST, "이미 방에 입장해 있습니다.");
        }

        // 방에 입장
        InGame inGame = InGame.builder()
                .room(room)
                .user(user)
                .score(0) // 초기 점수 0으로 설정
                .build();
        inGameRepository.save(inGame);

        // 방 입장 채팅 메시지 전송
        roomChatService.sendRoomEnterMessage(user, room.getId());

        // 현재 방 인원 수 다시 조회
        currentPlayerCount = inGameRepository.countByRoom(room);

        return JoinRoomResponse.builder()
                .roomId(room.getId())
                .roomName(room.getName())
                .hostName(room.getHost().getName())
                .currentPlayerCount((int) currentPlayerCount)
                .maxPlayer(room.getMaxPlayer())
                .gameStatus(currentStatus.name()) // GameSession의 상태 사용
                .build();
    }

    public GetRoomResponse getRoomById(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        Optional<GameSession> gameSessionOptional = gameSessionRepository.findById(roomId);
        GameStatus currentStatus = gameSessionOptional.map(GameSession::getGameStatus).orElse(GameStatus.WAITING);

        return responseConverter.from(room, currentStatus);
    }

    @Transactional
    public ExitRoomResponse leaveRoom(Long roomId, User user) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        InGame inGame = inGameRepository.findByRoomAndUser(room, user)
                .orElseThrow(() -> new IllegalArgumentException("방에 입장해 있지 않습니다."));

        // 퇴장 메시지 전송
        roomChatService.sendRoomLeaveMessage(user, roomId);

        // inGame은 user와 room을 동시에 가지고 있다.
        inGameRepository.delete(inGame);

        // 남은 인원을 확인
        Long restNumber = inGameRepository.countByRoom(room);

        // 방장 유/무 확인
        boolean isHost = room.getHost().getId().equals(user.getId());

        if (isHost) {
            if (restNumber > 0) {
                // 남은 사람이 있으면 첫 번째 인원에게 방장 권한을 위임
                User nextHost = inGameRepository.findAllByRoom(room).get(0).getUser();
                room.changeHost(nextHost);
                //roomRepository.save(room);

            } else {

                if (room.getRoomType() == RoomType.QUICK_MATCH) {
                    // 빠른대전 방 관련 데이터 삭제
                    quickMatchRepository.findByRoom(room).ifPresent(quickRoom -> {
                        quickMatchRoomPlayerRepository.deleteAllByRoom(quickRoom);
                        quickMatchRepository.delete(quickRoom);
                    });
                }
                // 남은 사람이 없으면 GameSession과 Room 삭제
                gameSessionRepository.findById(roomId).ifPresent(gameSessionRepository::delete);
                roomRepository.delete(room);
                return ExitRoomResponse.builder()
                                       .currentPlayer(0L)
                                       .gameStatus(GameStatus.DELETED)
                                       .users(List.of())
                                       .hostName(room.getHost().getName())
                                       .build();
            }
        }

        List<User> users = inGameRepository.findAllByRoom(room).stream()
                .map(InGame::getUser)
                .collect(Collectors.toList());

        // GameSession이 없으면 WAITING 상태로 간주
        GameStatus finalStatus = gameSessionRepository.findById(roomId)
                .map(GameSession::getGameStatus)
                .orElse(GameStatus.WAITING);

        return ExitRoomResponse.builder()
                .currentPlayer(restNumber)
                .gameStatus(finalStatus)
                .users(users)
                .build();
    }
}
