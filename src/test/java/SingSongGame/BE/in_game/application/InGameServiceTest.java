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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;


@ActiveProfiles("test")
//@Transactional
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
    AuthRepository userRepository; // for User persistence

    private User user1;
    private User user2;
    private User user3;
    private Room room;
    private Song song;

    @BeforeEach
    void setUp() {
    }

    @Transactional
    void saveEntity() {
        user1 = userRepository.save(User.builder().id(1l).email("kim@test.com").build());
        user2 = userRepository.save(User.builder().id(2l).email("lee@test.com").build());
        user3 = userRepository.save(User.builder().id(3l).email("park@test.com").build());

        room = Room.builder()
                                       .name("room")
                                       .roomType(null)
                                       .isPrivate(false)
                                       .password(0)
                                       .maxPlayer(4)
                                       .maxRound(3)
                                       .build();

        song = Song.builder()
                                       .title("t")
                                       .artist("a")
                                       .answer("test")
                                       .hint("h")
                                       .build();

        GameSession session = GameSession.builder()
                                         .room(room)
                                         .gameStatus(GameStatus.IN_PROGRESS)
                                         .currentRound(1)
                                         .maxRound(3)
                                         .currentSong(song)
                                         .roundStartTime(LocalDateTime.now())
                                         .build();
        InGame inGame1 = InGame.builder()
                                      .room(room)
                                      .user(user1)
                                      .score(0)
                                      .build();

        InGame inGame2 = InGame.builder()
                              .room(room)
                              .user(user2)
                              .score(0)
                              .build();

        InGame inGame3 = InGame.builder()
                              .room(room)
                              .user(user3)
                              .score(0)
                              .build();

        roomRepository.save(room);
        songRepository.save(song);
        gameSessionRepository.save(session);
        inGameRepository.save(inGame1);
        inGameRepository.save(inGame2);
        inGameRepository.save(inGame3);
    }

    @Test
    void 동시_정답_입력_시_동시성_문제_테스트() {
        saveEntity();

        int numberOfThreads = 3;

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        // 세 명의 유저가 동시에 정답 입력 -> 동시성 race condition 문제 발생
        Future<?> future1 = executorService.submit(() -> inGameService.verifyAnswer(user1, room.getId(), "test"));
        Future<?> future2 = executorService.submit(() -> inGameService.verifyAnswer(user2, room.getId(), "test"));
        Future<?> future3 = executorService.submit(() -> inGameService.verifyAnswer(user3, room.getId(), "test"));

        Exception result = new Exception();

        // 만약 동시성 문제를 @Version 필드가 잡았다면 아래와 같은 에러가 발생해야 됨.
        // 최초 커밋이 필드의 값을 변경함.
        try {
            future1.get();
            future2.get();
            future3.get();
        } catch (ExecutionException e) {
            result = (Exception) e.getCause();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertTrue(result instanceof OptimisticLockingFailureException);

    }
}