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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
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
    private final SimpMessagingTemplate messagingTemplate;

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

        // InGame 삭제
        inGameRepository.delete(inGame);

        // 남은 인원 확인
        Long restNumber = inGameRepository.countByRoom(room);

        // 방장인지 확인
        boolean isHost = room.getHost().getId().equals(user.getId());

        if (restNumber == 0) {
            // 마지막 플레이어가 나가면 방 완전 삭제
            deleteRoomCompletely(room);

            // 클라이언트에게 방 삭제 알림
            messagingTemplate.convertAndSend("/topic/rooms/deleted", roomId);

            return ExitRoomResponse.builder()
                    .currentPlayer(0L)
                    .gameStatus(GameStatus.DELETED)
                    .users(List.of())
                    .hostName(room.getHost().getName())
                    .build();

        } else if (isHost) {
            // 방장이 나가고 다른 사람이 남아있으면 방장 위임
            User nextHost = inGameRepository.findAllByRoom(room).get(0).getUser();
            room.changeHost(nextHost);
            // roomRepository.save(room); // @Transactional이 있어서 자동 저장됨

            // 방장 변경 알림
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/host-changed",
                    Map.of("newHostId", nextHost.getId(), "newHostName", nextHost.getName()));
        }

        // 현재 방 상태 조회
        List<User> users = inGameRepository.findAllByRoom(room).stream()
                .map(InGame::getUser)
                .collect(Collectors.toList());

        GameStatus finalStatus = gameSessionRepository.findById(roomId)
                .map(GameSession::getGameStatus)
                .orElse(GameStatus.WAITING);

        return ExitRoomResponse.builder()
                .currentPlayer(restNumber)
                .gameStatus(finalStatus)
                .users(users)
                .hostName(room.getHost().getName())
                .build();
    }

    // 방 완전 삭제를 위한 헬퍼 메서드 (RoomService에 추가)
    private void deleteRoomCompletely(Room room) {
        // 1. 빠른대전 관련 데이터 삭제
        if (room.getRoomType() == RoomType.QUICK_MATCH) {
            quickMatchRepository.findByRoom(room).ifPresent(quickRoom -> {
                quickMatchRoomPlayerRepository.deleteAllByRoom(quickRoom);
                quickMatchRepository.delete(quickRoom);
            });
        }

        // 2. GameSession 삭제
        gameSessionRepository.findById(room.getId()).ifPresent(gameSessionRepository::delete);

        // 3. Room 삭제
        roomRepository.delete(room);
    }


}
