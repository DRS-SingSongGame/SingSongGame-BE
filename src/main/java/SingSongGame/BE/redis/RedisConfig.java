package SingSongGame.BE.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    /*
    RedisMessageListenerContainer는 Redis Channel(Topic)로 부터 메시지를 받고,
    주입된 리스너들에게 비동기적으로 dispatch 하는 역할을 수행하는 컨테이너이다.
    즉, 발행된 메시지 처리를 위한 리스너들을 설정할 수 있다.
     */

    /*
    로비 채팅을 위한 RedisMessageListenerContainer를 추가한다.
     */
    @Bean
    public RedisMessageListenerContainer lobbyRedisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter lobbyListenerAdapter,
            ChannelTopic lobbyChannelTopic
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(lobbyListenerAdapter, lobbyChannelTopic);
        return container;
    }

    /*
    MessageListenerAdaper에서는 RedisMessageListenerContainer로부터 메시지를 dispatch 받고,
    실제 메시지를 처리하는 비즈니스 로직이 담긴 Subscriber Bean을 추가해준다.
     */
//    @Bean
//    public MessageListenerAdapter listenerAdapter(RedisSubscriber subscriber) {
//        return new MessageListenerAdapter(subscriber, "onMessage");
//    }

    /*
    로비 채팅을 위한 MessageListenerAdapter를 추가한다.
     */
    @Bean
    public MessageListenerAdapter lobbyListenerAdapter(LobbyRedisSubscriber lobbySubscriber) {
        return new MessageListenerAdapter(lobbySubscriber, "onMessage");
    }

    /*
    Redis서버와 상호작용하기 위한 RedisTemplate 관련 설정을 해준다.
    Redis 서버에는 bytes 코드만이 저장되므로 key와 value에 Serializer를 설정해준다.
    Json 포맷 형식으로 메시지를 교환하기 위해 ValueSerializer에 Jackson2JsonRedisSerializer로 설정해준다.
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate
            (RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));
        return redisTemplate;
    }

    /*
    Topic 공유를 위해 Channel Topic을 빈으로 등록해 단일화 시켜준다.
     */
//    @Bean
//    public ChannelTopic channelTopic() {
//        return new ChannelTopic("chatroom");
//    }

    /*
    로비 채팅을 위한 Channel Topic을 추가한다.
     */
    @Bean
    public ChannelTopic lobbyChannelTopic() {
        return new ChannelTopic("/topic/lobby");
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
