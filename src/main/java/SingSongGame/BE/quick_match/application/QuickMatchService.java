package SingSongGame.BE.quick_match.application;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.chat.service.QuickMatchMessageService;
import SingSongGame.BE.in_game.application.InGameService;
import SingSongGame.BE.quick_match.application.rating.TierChangeResult;
import SingSongGame.BE.quick_match.persistence.QuickMatchRepository;
import SingSongGame.BE.quick_match.persistence.QuickMatchRoom;
import SingSongGame.BE.quick_match.persistence.QuickMatchRoomPlayer;
import SingSongGame.BE.quick_match.persistence.QuickMatchRoomPlayerRepository;
import SingSongGame.BE.room.persistence.Room;
import SingSongGame.BE.room.persistence.RoomPlayer;
import SingSongGame.BE.room.persistence.RoomRepository;
import SingSongGame.BE.room.persistence.RoomType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import SingSongGame.BE.quick_match.application.dto.response.QuickMatchResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuickMatchService {

    private final QuickMatchRepository quickMatchRepository;
    private final QuickMatchRoomPlayerRepository quickMatchRoomPlayerRepository;
    private final RoomRepository roomRepository;
    private final InGameService inGameService;
    private final SimpMessageSendingOperations messagingTemplate;
    private final QuickMatchMessageService quickMatchMessageService;
    private final QuickMatchResultService quickMatchResultService;

    public QuickMatchRoom createQuickMatchRoom(List<User> users) {
        int averageMmr = (int) users.stream()
                .mapToInt(User::getQuickMatchMmr)
                .average()
                .orElse(1000);

        Room room = Room.builder()
                .roomType(RoomType.QUICK_MATCH)
                .createdAt(LocalDateTime.now())
                .host(users.get(0))
                .build();
        roomRepository.save(room);

        QuickMatchRoom quickRoom = QuickMatchRoom.builder()
                .roomCode(generateRoomCode())
                .createdAt(LocalDateTime.now())
                .gameStarted(false)
                .gameEnded(false)
                .roundCount(5)
                .mode("RANDOMSONG")
                .averageMmr(averageMmr)
                .room(room)
                .build();
        quickMatchRepository.save(quickRoom);
        room.setQuickMatchRoom(quickRoom);

        // ✅ (1) RoomPlayer 생성 및 저장
        List<RoomPlayer> roomPlayers = users.stream()
                .map(user -> RoomPlayer.builder()
                        .room(room)
                        .user(user)
                        .score(0)
                        .build())
                .toList();
        room.getPlayers().addAll(roomPlayers); // 양방향 설정
        roomRepository.save(room); // 또는 별도로 roomPlayerRepository.saveAll(roomPlayers);

        // ✅ (2) QuickMatchRoomPlayer 생성 및 저장
        List<QuickMatchRoomPlayer> quickPlayers = users.stream()
                .map(user -> QuickMatchRoomPlayer.builder()
                        .room(quickRoom)
                        .user(user)
                        .mmrAtMatchTime(user.getQuickMatchMmr()) // 선택사항
                        .build())
                .toList();
        quickRoom.getPlayers().addAll(quickPlayers); // 양방향 설정
        quickMatchRoomPlayerRepository.saveAll(quickPlayers);

        return quickRoom;
    }

    public void startQuickMatchGame(List<User> users) {
        QuickMatchRoom quickRoom = createQuickMatchRoom(users);

        users.forEach(user -> {
            quickMatchMessageService.sendMatchFoundMessage(
                    user,
                    QuickMatchResponse.from(quickRoom) // 👈 방 정보 DTO로 변환 필요
            );
        });
        String keyword = "전체";
        inGameService.startGame(quickRoom.getRoom().getId(), Set.of("전체")); // 🎯 연결된 Room ID로 게임 시작
    }


    public QuickMatchRoom findByRoomCode(String roomCode) {
        return quickMatchRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("해당 roomCode의 방을 찾을 수 없습니다."));
    }

    @Transactional
    public List<TierChangeResult> endGame(QuickMatchRoom room) {
        // 1. 게임 세션 종료
        inGameService.endGame(room.getRoom().getId());

        // 2. QuickMatchRoom 상태 업데이트
        room.setGameEnded(true);
        room.setGameStarted(false);
        quickMatchRepository.save(room);

        // 3. 클라이언트에게도 종료 알림 (선택)
        messagingTemplate.convertAndSend(
                "/topic/room/" + room.getRoom().getId() + "/game-end",
                Map.of("message", "빠른 매칭 게임이 종료되었습니다!")
        );
        List<QuickMatchRoomPlayer> quickPlayers = room.getPlayers();
        return quickMatchResultService.processQuickMatchResult(quickPlayers);
    }

    private String generateRoomCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

}
