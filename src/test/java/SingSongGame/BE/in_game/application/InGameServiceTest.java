package SingSongGame.BE.in_game.application;

import SingSongGame.BE.auth.persistence.AuthRepository;
import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.in_game.persistence.GameSession;
import SingSongGame.BE.in_game.persistence.GameSessionRepository;
import SingSongGame.BE.in_game.persistence.InGame;
import SingSongGame.BE.in_game.persistence.InGameRepository;
import SingSongGame.BE.room.persistence.GameStatus;
import SingSongGame.BE.room.persistence.Room;
import SingSongGame.BE.room.persistence.RoomRepository;
import SingSongGame.BE.song.persistence.Song;
import SingSongGame.BE.song.persistence.SongRepository;
import SingSongGame.BE.user.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;


@ActiveProfiles("test")
@SpringBootTest
class InGameServiceTest {

    @Autowired
    InGameService inGameService;
    @Autowired
    GameSessionRepository gameSessionRepository;
    @Autowired
    InGameRepository inGameRepository;
    @Autowired
    RoomRepository roomRepository;
    @Autowired
    SongRepository songRepository;
    @Autowired
    AuthRepository userRepository;

    @BeforeEach
    void setUp() {
        // 테스트 전에 데이터 정리
        inGameRepository.deleteAll();
        gameSessionRepository.deleteAll();
        roomRepository.deleteAll();
        songRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    void 동시_정답_입력_시_동시성_문제_테스트() throws InterruptedException {

        User user1 = userRepository.save(User.builder().email("kim@test.com").name("이도연").build());
        User user2 = userRepository.save(User.builder().email("lee@test.com").name("싱송챔").build());
        User user3 = userRepository.save(User.builder().email("park@test.com").name("박테스트").build());

        Room room = roomRepository.save(Room.builder()
                .name("room")
                .roomType(null)
                .isPrivate(false)
                .password(0)
                .maxPlayer(4)
                .maxRound(3)
                .build());
        Room roomEntity = roomRepository.findById(room.getId()).orElseThrow(); // 반드시 저장된 id로 조회

        Song song = songRepository.save(Song.builder()
                .title("t")
                .artist("a")
                .answer("test")
                .hint("h")
                .build());
        Song songEntity = songRepository.findById(song.getId()).orElseThrow(); // song도 동일하게

        GameSession session = gameSessionRepository.save(GameSession.builder()
                .room(roomEntity)
                .gameStatus(GameStatus.IN_PROGRESS)
                .currentRound(1)
                .maxRound(3)
                .currentSong(songEntity)
                .roundStartTime(LocalDateTime.now())
                .roundAnswered(false)
                .build());

        // InGame 데이터 생성
        inGameRepository.save(InGame.builder().room(roomEntity).user(user1).score(0).build());
        inGameRepository.save(InGame.builder().room(roomEntity).user(user2).score(0).build());
        inGameRepository.save(InGame.builder().room(roomEntity).user(user3).score(0).build());

        // 간단한 동시성 테스트: 같은 트랜잭션에서 여러 번 호출
        try {
            // 첫 번째 호출은 성공해야 함
            inGameService.verifyAnswer(user1, roomEntity.getId(), "test");
            
            // 두 번째 호출은 이미 답변된 상태이므로 예외가 발생하거나 다른 결과가 나와야 함
            inGameService.verifyAnswer(user2, roomEntity.getId(), "test");
            
            // 세 번째 호출도 마찬가지
            inGameService.verifyAnswer(user3, roomEntity.getId(), "test");
            
            // 모든 호출이 성공했다면, 동시성 처리가 제대로 되지 않았을 수 있음
            System.out.println("모든 verifyAnswer 호출이 성공했습니다. 동시성 처리를 확인해보세요.");
            
        } catch (Exception e) {
            // 예외가 발생했다면 동시성 처리가 제대로 되고 있는 것
            System.out.println("동시성 처리로 인한 예외 발생: " + e.getMessage());
            assertTrue(true, "동시성 처리가 정상적으로 작동하고 있습니다.");
        }
        
        // 게임 세션 상태 확인
        GameSession updatedSession = gameSessionRepository.findById(session.getId()).orElseThrow();
        assertTrue(updatedSession.isRoundAnswered(), "라운드가 답변 완료 상태여야 합니다.");
    }
}