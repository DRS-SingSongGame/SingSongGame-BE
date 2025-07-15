package SingSongGame.BE.online.persistence;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class OnlineUserRepository {
    private final Map<Long, OnlineUser> onlineUserMap = new ConcurrentHashMap<>();

    public void save(OnlineUser user) {
        onlineUserMap.put(user.getUserId(), user);
    }

    public void delete(Long userId) {
        onlineUserMap.remove(userId);
    }

    public OnlineUser findById(Long userId) {
        return onlineUserMap.get(userId);
    }

    public Map<Long, OnlineUser> findAll() {
        return onlineUserMap;
    }

}
