package SingSongGame.BE.chat.config;

import SingSongGame.BE.chat.StompHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class StompConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;

    /*
    소켓 연결과 관련된 설정
    addEndpoint() 메서드에 넣어주는 String 값은 소켓 연결 uri
    setAllowedOriginPatterns() 메서드는 소켓 CORS 설정
    withSockJS() 메서드는 소켓을 지원하지 않는 브라우저라면, sockJS를 사용하도록 설정
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat")
                .setHandshakeHandler(new CustomHandshakeHandler())
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /*
    STOMP 사용을 휘한 Message Broker 설정
    enableSimpleBroker() 메서드는 메세지를 받을 때, 경로를 설정해주는 함수, 스프링에서 제공하는 내장 브로커를 제공
    setApplicationDestinationPrefixes() 메서드는 메세지를 보낼 때, 관련 경로를 설정해주는 함수
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/queue", "/topic");

        // server.servlet.context-path 이미 프로퍼티에 설정해 두었는데 이게 중복 되려나?
        registry.setApplicationDestinationPrefixes("/api");
    }

    // 없어도 될 듯
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }
}
