package SingSongGame.BE.room.application;

import SingSongGame.BE.in_game.persistence.GameSession;
import SingSongGame.BE.in_game.persistence.GameSessionRepository;
import SingSongGame.BE.in_game.persistence.InGameRepository;
import SingSongGame.BE.quick_match.persistence.QuickMatchRepository;
import SingSongGame.BE.quick_match.persistence.QuickMatchRoomPlayerRepository;
import SingSongGame.BE.room.persistence.GameStatus;
import SingSongGame.BE.room.persistence.Room;
import SingSongGame.BE.room.persistence.RoomRepository;
import SingSongGame.BE.room.persistence.RoomType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomCleanupScheduler {
    private final RoomRepository roomRepository;
    private final InGameRepository inGameRepository;
    private final GameSessionRepository gameSessionRepository;
    private final QuickMatchRepository quickMatchRepository;
    private final QuickMatchRoomPlayerRepository quickMatchRoomPlayerRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedRate = 30000) // 30초마다 실행
    @Transactional
    public void cleanupEmptyRooms() {
        log.info("빈 방 정리 작업 시작");

        List<Room> allRooms = roomRepository.findAll();
        int deletedCount = 0;

        for (Room room : allRooms) {
            long playerCount = inGameRepository.countByRoom(room);

            // 플레이어가 없는 방 삭제
            if (playerCount == 0) {
                try {
                    deleteRoomCompletely(room);

                    // 클라이언트에게 방 삭제 알림
                    messagingTemplate.convertAndSend("/topic/rooms/deleted", room.getId());

                    deletedCount++;
                    log.info("빈 방 삭제: roomId={}, roomName={}", room.getId(), room.getName());

                } catch (Exception e) {
                    log.error("방 삭제 중 오류 발생: roomId={}, error={}", room.getId(), e.getMessage());
                }
            }
        }

        if (deletedCount > 0) {
            log.info("빈 방 정리 완료: {}개 방 삭제", deletedCount);
        }
    }

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    @Transactional
    public void cleanupStaleGameSessions() {
        log.info("오래된 게임 세션 정리 작업 시작");

        List<GameSession> staleSessions = gameSessionRepository.findAll().stream()
                .filter(session -> {
                    // 2시간 이상 진행중인 게임은 강제 종료
                    if (session.getGameStatus() == GameStatus.IN_PROGRESS) {
                        return session.getCreatedAt().isBefore(LocalDateTime.now().minusHours(2));
                    }
                    return false;
                })
                .toList();

        for (GameSession session : staleSessions) {
            try {
                session.setGameStatus(GameStatus.WAITING);
                gameSessionRepository.save(session);

                // 해당 방의 플레이어들에게 알림
                messagingTemplate.convertAndSend(
                        "/topic/room/" + session.getId() + "/force-ended",
                        "게임이 너무 오래 진행되어 자동으로 종료되었습니다."
                );

                log.info("오래된 게임 세션 종료: sessionId={}", session.getId());

            } catch (Exception e) {
                log.error("게임 세션 정리 중 오류 발생: sessionId={}, error={}", session.getId(), e.getMessage());
            }
        }
    }

    private void deleteRoomCompletely(Room room) {
        // 1. InGame 데이터 먼저 삭제 (외래키 제약 때문에)
        inGameRepository.deleteAllByRoom(room);

        // 2. 빠간대전 관련 데이터 삭제
        if (room.getRoomType() == RoomType.QUICK_MATCH) {
            quickMatchRepository.findByRoom(room).ifPresent(quickRoom -> {
                quickMatchRoomPlayerRepository.deleteAllByRoom(quickRoom);
                quickMatchRepository.delete(quickRoom);
            });
        }

        // 3. GameSession 삭제
        gameSessionRepository.findById(room.getId()).ifPresent(gameSessionRepository::delete);

        // 4. Room 삭제 (마지막에)
        roomRepository.delete(room);
    }
}
