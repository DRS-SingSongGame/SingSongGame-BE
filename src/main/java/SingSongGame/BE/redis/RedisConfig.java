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
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {



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
            @Qualifier("inGameChannelTopic") ChannelTopic inGameChannelTopic
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(inGameListenerAdapter, inGameChannelTopic);
        return container;
    }

    @Bean("inGameListenerAdapter")
    public MessageListenerAdapter inGameListenerAdapter(InGameRedisSubscriber inGameSubscriber) {
        return new MessageListenerAdapter(inGameSubscriber, "onMessage");
    }


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


    @Bean("lobbyChannelTopic")
    public ChannelTopic lobbyChannelTopic() {
        return new ChannelTopic("/topic/lobby");
    }

    @Bean("inGameChannelTopic")
    public ChannelTopic inGameChannelTopic() {
        return new ChannelTopic("/topic/room/*/answer");
    }


    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
