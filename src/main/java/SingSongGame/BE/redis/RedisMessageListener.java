package SingSongGame.BE.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class RedisMessageListener {
    private static final Map<Long, ChannelTopic> TOPICS = new HashMap<>();
    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final LobbyRedisSubscriber redisSubscriber;

    public void enterChatRoom(Long roomId) {
        ChannelTopic topic = getTopic(roomId);

        if (topic == null) {
            topic = new ChannelTopic(String.valueOf(roomId));
            redisMessageListenerContainer.addMessageListener(redisSubscriber, topic);
            TOPICS.put(roomId, topic);
        }
    }

    public void deleteChatRoom(Long roomId) {
        redisMessageListenerContainer.removeMessageListener(redisSubscriber, getTopic(roomId));
        TOPICS.remove(roomId);
    }

    public ChannelTopic getTopic(Long roomId) {
        return TOPICS.get(roomId);
    }

}
