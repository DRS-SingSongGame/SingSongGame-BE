package SingSongGame.BE.in_game.application;

import SingSongGame.BE.in_game.dto.request.AnswerSubmission;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnswerQueueService {

    private final RedissonClient redissonClient;
    private final ApplicationContext applicationContext;

    private RBlockingQueue<AnswerSubmission> queue;
    private final Map<String, CompletableFuture<Boolean>> futures = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        queue = redissonClient.getBlockingQueue("answer:queue");
        Thread worker = new Thread(this::processQueue);
        worker.setDaemon(true);
        worker.start();
    }

    private void processQueue() {
        while (true) {
            try {
                AnswerSubmission submission = queue.take();
                boolean result = applicationContext.getBean(InGameService.class).processAnswer(submission);
                CompletableFuture<Boolean> future = futures.remove(submission.getRequestId());
                if (future != null) {
                    future.complete(result);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error processing answer", e);
            }
        }
    }

    public boolean submitAnswer(AnswerSubmission submission) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        futures.put(submission.getRequestId(), future);
        queue.add(submission);
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (ExecutionException e) {
            log.error("Error waiting for answer result", e);
            return false;
        } finally {
            futures.remove(submission.getRequestId());
        }
    }
}