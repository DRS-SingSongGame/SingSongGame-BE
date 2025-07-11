package SingSongGame.BE.room_keyword;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KeywordService {

    // roomId → tagId 목록
    private final Map<Long, List<Long>> keywordMap = new ConcurrentHashMap<>();

    public void saveKeywords(Long roomId, List<Long> keywords) {
        keywordMap.put(roomId, keywords);
    }

    public List<Long> getKeywords(Long roomId) {
        return keywordMap.getOrDefault(roomId, List.of());
    }

    public void clearKeywords(Long roomId) {
        keywordMap.remove(roomId);
    }

    public boolean hasKeywords(Long roomId) {
        return keywordMap.containsKey(roomId);
    }
}
