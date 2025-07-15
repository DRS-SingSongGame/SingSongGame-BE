package SingSongGame.BE.online.persistence;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionUserRegistry {
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    public void register(String sessionId, Long userId) {
        sessionUserMap.put(sessionId, userId);
    }

    public Long getUserIdBySessionId(String sessionId) {
        return sessionUserMap.get(sessionId);
    }

    public void remove(String sessionId) {
        sessionUserMap.remove(sessionId);
    }
}

