package SingSongGame.BE.chat;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.chat.dto.RoomChatMessage;
import SingSongGame.BE.chat.service.AiGameChatService;
import SingSongGame.BE.in_game.application.InGameChatService;
import SingSongGame.BE.online.persistence.OnlineUserRepository;
import SingSongGame.BE.room.persistence.Room;
import SingSongGame.BE.room.persistence.RoomRepository;
import SingSongGame.BE.song.application.SongCsvLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestWebSocketConfig.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class ChatServicePerformanceTest {

    @Autowired
    private InGameChatService inGameChatService;

    @Autowired
    private AiGameChatService aiGameChatService;

    @MockBean
    private RoomRepository roomRepository;

    @MockBean
    private SimpMessageSendingOperations sendingOperations;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @MockBean
    private OnlineUserRepository onlineUserRepository;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Qualifier("inGameRedisMessageListenerContainer")
    @Autowired
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @MockBean
    private SongCsvLoader songCsvLoader;

    @Autowired
    private ObjectMapper objectMapper;

    private Room testRoom;
    private User testUser;
    private List<Long> messageLatencies = Collections.synchronizedList(new ArrayList<>());

    @BeforeEach
    void setUp() {
        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setName("Test Room");

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
    }

    @Test
    @DisplayName("Redis Pub/Sub vs Spring 내장 메시지 브로커 성능 비교 - 단일 메시지")
    void compareSingleMessagePerformance() throws InterruptedException {
        // Redis Pub/Sub 테스트
        long redisStartTime = System.nanoTime();
        inGameChatService.verifyAnswer(testUser, 1L, "test message");
        long redisEndTime = System.nanoTime();
        long redisLatency = (redisEndTime - redisStartTime) / 1_000_000; // ms 변환

        // Spring 내장 메시지 브로커 테스트
        long springStartTime = System.nanoTime();
        aiGameChatService.sendRoomMessage(testUser, 1L, "test message");
        long springEndTime = System.nanoTime();
        long springLatency = (springEndTime - springStartTime) / 1_000_000; // ms 변환

        System.out.println("=== 단일 메시지 전송 성능 비교 ===");
        System.out.println("Redis Pub/Sub 지연 시간: " + redisLatency + " ms");
        System.out.println("Spring 내장 브로커 지연 시간: " + springLatency + " ms");
        System.out.println("성능 차이: " + Math.abs(redisLatency - springLatency) + " ms");
    }

    @Test
    @DisplayName("Redis Pub/Sub vs Spring 내장 메시지 브로커 성능 비교 - 부하 테스트")
    void compareLoadTestPerformance() throws InterruptedException {
        int messageCount = 10000;
        int threadCount = 10;

        // Redis Pub/Sub 부하 테스트
        System.out.println("\n=== Redis Pub/Sub 부하 테스트 ===");
        long redisResults = performLoadTest(messageCount, threadCount, true);

        // 테스트 간 쿨다운
        Thread.sleep(2000);

        // Spring 내장 메시지 브로커 부하 테스트
        System.out.println("\n=== Spring 내장 브로커 부하 테스트 ===");
        long springResults = performLoadTest(messageCount, threadCount, false);

        System.out.println("\n=== 부하 테스트 결과 비교 ===");
        System.out.println("Redis Pub/Sub 총 소요 시간: " + redisResults + " ms");
        System.out.println("Spring 내장 브로커 총 소요 시간: " + springResults + " ms");
        System.out.println("성능 차이: " + Math.abs(redisResults - springResults) + " ms");
    }

    @Test
    @DisplayName("Redis Pub/Sub vs Spring 내장 메시지 브로커 성능 비교 - 동시성 테스트")
    void compareConcurrencyPerformance() throws InterruptedException {
        int concurrentUsers = 50;
        int messagesPerUser = 20;

        System.out.println("\n=== 동시성 테스트 설정 ===");
        System.out.println("동시 사용자 수: " + concurrentUsers);
        System.out.println("사용자당 메시지 수: " + messagesPerUser);

        // Redis Pub/Sub 동시성 테스트
        System.out.println("\n=== Redis Pub/Sub 동시성 테스트 ===");
        ConcurrencyTestResult redisResult = performConcurrencyTest(concurrentUsers, messagesPerUser, true);

        Thread.sleep(2000);

        // Spring 내장 메시지 브로커 동시성 테스트
        System.out.println("\n=== Spring 내장 브로커 동시성 테스트 ===");
        ConcurrencyTestResult springResult = performConcurrencyTest(concurrentUsers, messagesPerUser, false);

        // 결과 비교
        System.out.println("\n=== 동시성 테스트 결과 비교 ===");
        System.out.println("Redis Pub/Sub:");
        System.out.println("  - 평균 지연 시간: " + redisResult.avgLatency + " ms");
        System.out.println("  - 최소 지연 시간: " + redisResult.minLatency + " ms");
        System.out.println("  - 최대 지연 시간: " + redisResult.maxLatency + " ms");
        System.out.println("  - 처리량: " + redisResult.throughput + " msg/s");

        System.out.println("\nSpring 내장 브로커:");
        System.out.println("  - 평균 지연 시간: " + springResult.avgLatency + " ms");
        System.out.println("  - 최소 지연 시간: " + springResult.minLatency + " ms");
        System.out.println("  - 최대 지연 시간: " + springResult.maxLatency + " ms");
        System.out.println("  - 처리량: " + springResult.throughput + " msg/s");
    }

    @Test
    @DisplayName("메시지 크기별 성능 비교")
    void comparePerformanceByMessageSize() throws InterruptedException {
        int[] messageSizes = {10, 100, 1000, 10000}; // bytes

        System.out.println("\n=== 메시지 크기별 성능 비교 ===");

        for (int size : messageSizes) {
            String message = generateMessage(size);

            // Redis 테스트
            long redisStartTime = System.nanoTime();
            inGameChatService.verifyAnswer(testUser, 1L, message);
            long redisLatency = (System.nanoTime() - redisStartTime) / 1_000_000;

            Thread.sleep(100);

            // Spring 테스트
            long springStartTime = System.nanoTime();
            aiGameChatService.sendRoomMessage(testUser, 1L, message);
            long springLatency = (System.nanoTime() - springStartTime) / 1_000_000;

            System.out.println("\n메시지 크기: " + size + " bytes");
            System.out.println("Redis Pub/Sub: " + redisLatency + " ms");
            System.out.println("Spring 브로커: " + springLatency + " ms");
        }
    }

    private long performLoadTest(int messageCount, int threadCount, boolean useRedis) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(messageCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < messageCount; i++) {
            final int messageIndex = i;
            executor.submit(() -> {
                try {
                    if (useRedis) {
                        inGameChatService.verifyAnswer(testUser, 1L, "message-" + messageIndex);
                    } else {
                        aiGameChatService.sendRoomMessage(testUser, 1L, "message-" + messageIndex);
                    }
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        System.out.println("총 메시지 수: " + messageCount);
        System.out.println("성공: " + successCount.get());
        System.out.println("실패: " + failureCount.get());
        System.out.println("총 소요 시간: " + totalTime + " ms");
        System.out.println("평균 처리 시간: " + (totalTime / (double) messageCount) + " ms/msg");
        System.out.println("처리량: " + (messageCount / (totalTime / 1000.0)) + " msg/s");

        return totalTime;
    }

    private ConcurrencyTestResult performConcurrencyTest(int userCount, int messagesPerUser, boolean useRedis) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(userCount);
        CountDownLatch latch = new CountDownLatch(userCount * messagesPerUser);
        List<Long> latencies = Collections.synchronizedList(new ArrayList<>());
        AtomicLong totalLatency = new AtomicLong(0);

        long testStartTime = System.currentTimeMillis();

        for (int userId = 0; userId < userCount; userId++) {
            final int userIdFinal = userId;
            executor.submit(() -> {
                User user = new User();
                user.setId((long) userIdFinal);
                user.setName("User-" + userIdFinal);
                user.setEmail("user" + userIdFinal + "@example.com");

                for (int msgId = 0; msgId < messagesPerUser; msgId++) {
                    long msgStartTime = System.nanoTime();
                    try {
                        if (useRedis) {
                            inGameChatService.verifyAnswer(user, 1L, "user-" + userIdFinal + "-msg-" + msgId);
                        } else {
                            aiGameChatService.sendRoomMessage(user, 1L, "user-" + userIdFinal + "-msg-" + msgId);
                        }
                        long latency = (System.nanoTime() - msgStartTime) / 1_000_000;
                        latencies.add(latency);
                        totalLatency.addAndGet(latency);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }

        latch.await(120, TimeUnit.SECONDS);
        executor.shutdown();

        long testEndTime = System.currentTimeMillis();
        long totalTestTime = testEndTime - testStartTime;
        int totalMessages = userCount * messagesPerUser;

        // 통계 계산
        double avgLatency = latencies.isEmpty() ? 0 : totalLatency.get() / (double) latencies.size();
        long minLatency = latencies.isEmpty() ? 0 : Collections.min(latencies);
        long maxLatency = latencies.isEmpty() ? 0 : Collections.max(latencies);
        double throughput = totalMessages / (totalTestTime / 1000.0);

        return new ConcurrencyTestResult(avgLatency, minLatency, maxLatency, throughput);
    }

    private String generateMessage(int sizeInBytes) {
        StringBuilder sb = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();

        for (int i = 0; i < sizeInBytes; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString();
    }

    private static class ConcurrencyTestResult {
        final double avgLatency;
        final long minLatency;
        final long maxLatency;
        final double throughput;

        ConcurrencyTestResult(double avgLatency, long minLatency, long maxLatency, double throughput) {
            this.avgLatency = avgLatency;
            this.minLatency = minLatency;
            this.maxLatency = maxLatency;
            this.throughput = throughput;
        }
    }
}