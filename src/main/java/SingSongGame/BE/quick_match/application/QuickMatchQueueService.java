package SingSongGame.BE.quick_match.application;

import SingSongGame.BE.auth.persistence.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class QuickMatchQueueService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String QUEUE_KEY = "quick_match_queue";

    public void addToQueue(User user) {
        redisTemplate.opsForZSet().add(QUEUE_KEY, user.getId(), user.getQuickMatchMmr());
    }

    public void removeFromQueue(Long userId) {
        redisTemplate.opsForZSet().remove(QUEUE_KEY, userId);
    }

    public Set<Object> getCandidatesInRange(int minMmr, int maxMmr) {
        return redisTemplate.opsForZSet().rangeByScore(QUEUE_KEY, minMmr, maxMmr);
    }

    public Long getQueueSize() {
        return redisTemplate.opsForZSet().size(QUEUE_KEY);
    }
}
