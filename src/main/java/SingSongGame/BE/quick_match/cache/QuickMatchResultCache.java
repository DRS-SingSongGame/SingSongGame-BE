package SingSongGame.BE.quick_match.cache;

import SingSongGame.BE.quick_match.application.rating.TierChangeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class QuickMatchResultCache {

    // 🔧 Long -> String 으로 변경
    private final Map<String, List<TierChangeResult>> cache = new ConcurrentHashMap<>();

    public void put(String roomCode, List<TierChangeResult> results) {
        cache.put(roomCode, results);
    }

    public List<TierChangeResult> get(String roomCode) {
        return cache.get(roomCode);
    }

    public void clear(String roomCode) {
        cache.remove(roomCode);
    }
}
