package SingSongGame.BE.in_game.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameScheduler {
    
    private final TaskScheduler taskScheduler;
    private final SimpMessageSendingOperations messagingTemplate;
    private final ApplicationContext applicationContext;
    
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new HashMap<>();
    
    public void scheduleGameStart(Long roomId, int countdownSeconds) {
        ScheduledFuture<?> future = taskScheduler.schedule(
            () -> applicationContext.getBean(InGameService.class).startNextRound(roomId),
            new Date(System.currentTimeMillis() + countdownSeconds * 1000)
        );
        scheduledTasks.put(roomId, future);
    }
    
    public void scheduleRoundEnd(Long roomId, int roundDurationSeconds) {
        ScheduledFuture<?> future = taskScheduler.schedule(() -> {
            InGameService inGameService = applicationContext.getBean(InGameService.class);
            GameStateManager gameStateManager = applicationContext.getBean(GameStateManager.class);
            
            var gameSession = gameStateManager.getGameSession(roomId);
            
            if (!gameSession.isRoundAnswered()) {
                messagingTemplate.convertAndSend(
                    "/topic/room/" + roomId + "/round-failed",
                    Map.of("title", gameSession.getCurrentSong().getTitle())
                );
                
                scheduleDelayedNextRound(roomId, 3000);
            } else {
                inGameService.startNextRound(roomId);
            }
        }, new Date(System.currentTimeMillis() + roundDurationSeconds * 1000));
        
        scheduledTasks.put(roomId, future);
    }
    
    public void scheduleDelayedNextRound(Long roomId, int delayMillis) {
        ScheduledFuture<?> delayTask = taskScheduler.schedule(
            () -> applicationContext.getBean(InGameService.class).startNextRound(roomId),
            new Date(System.currentTimeMillis() + delayMillis)
        );
        scheduledTasks.put(roomId, delayTask);
    }
    
    public void scheduleAnswerReveal(Long roomId, int revealDurationSeconds) {
        ScheduledFuture<?> nextRoundTask = taskScheduler.schedule(
            () -> applicationContext.getBean(InGameService.class).startNextRound(roomId),
            new Date(System.currentTimeMillis() + revealDurationSeconds * 1000)
        );
        scheduledTasks.put(roomId, nextRoundTask);
    }
    
    public void cancelScheduledTask(Long roomId) {
        ScheduledFuture<?> currentTask = scheduledTasks.get(roomId);
        if (currentTask != null) {
            currentTask.cancel(false);
            scheduledTasks.remove(roomId);
        }
    }
    
    public void clearAllTasks(Long roomId) {
        cancelScheduledTask(roomId);
    }
}