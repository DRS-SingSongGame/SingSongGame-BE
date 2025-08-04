package SingSongGame.BE.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisMessageListenerContainer roomChatRedisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            @Qualifier("roomChatListenerAdapter") MessageListenerAdapter roomChatListenerAdapter,
            @Qualifier("roomChatPatternTopic") PatternTopic roomChatPatternTopic
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(roomChatListenerAdapter, roomChatPatternTopic);
        return container;
    }

    @Bean("roomChatListenerAdapter")
    public MessageListenerAdapter roomChatListenerAdapter(RoomChatRedisSubscriber roomChatSubscriber) {
        return new MessageListenerAdapter(roomChatSubscriber, "onMessage");
    }

    @Bean
    public RedisMessageListenerContainer lobbyRedisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            @Qualifier("lobbyListenerAdapter") MessageListenerAdapter lobbyListenerAdapter,
            @Qualifier("lobbyChannelTopic") ChannelTopic lobbyChannelTopic
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(lobbyListenerAdapter, lobbyChannelTopic);
        return container;
    }

    @Bean("lobbyListenerAdapter")
    public MessageListenerAdapter lobbyListenerAdapter(LobbyRedisSubscriber lobbySubscriber) {
        return new MessageListenerAdapter(lobbySubscriber, "onMessage");
    }

    @Bean
    public RedisMessageListenerContainer inGameRedisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            @Qualifier("inGameListenerAdapter") MessageListenerAdapter inGameListenerAdapter,
            @Qualifier("inGamePatternTopic") PatternTopic inGamePatternTopic
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(inGameListenerAdapter, inGamePatternTopic);
        return container;
    }

    @Bean("inGameListenerAdapter")
    public MessageListenerAdapter inGameListenerAdapter(InGameRedisSubscriber inGameSubscriber) {
        return new MessageListenerAdapter(inGameSubscriber, "onMessage");
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        
        // ObjectMapper 설정 (JavaTimeModule 포함)
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
        redisTemplate.setValueSerializer(serializer);
        
        return redisTemplate;
    }

    @Bean("lobbyChannelTopic")
    public ChannelTopic lobbyChannelTopic() {
        return new ChannelTopic("/topic/lobby");
    }

    // PatternTopic 사용 (와일드카드 지원)
    @Bean("inGamePatternTopic")
    public PatternTopic inGamePatternTopic() {
        return new PatternTopic("/topic/room/*/answer-correct");
    }

    // 채팅용 PatternTopic
    @Bean("roomChatPatternTopic")
    public PatternTopic roomChatPatternTopic() {
        return new PatternTopic("/topic/room/*/chat");
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
