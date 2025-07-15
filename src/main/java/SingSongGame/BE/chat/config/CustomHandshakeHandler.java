package SingSongGame.BE.chat.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Map;

public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();

            String query = httpServletRequest.getQueryString(); // ?userId=1&nickname=홍길동
            String userIdStr = null;
            String nickname = null;

            if (query != null) {
                for (String param : query.split("&")) {
                    String[] parts = param.split("=");
                    if (parts.length == 2) {
                        String key = parts[0];
                        String value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8); // ✅ URL decode

                        if ("userId".equals(key)) {
                            userIdStr = value;
                        } else if ("nickname".equals(key)) {
                            nickname = value;
                        }
                    }
                }
            }

            if (userIdStr != null && nickname != null) {
                try {
                    Long userId = Long.parseLong(userIdStr);
                    System.out.println("✅ Principal 등록됨: userId=" + userId + ", nickname=" + nickname);
                    return new StompPrincipal(userId, nickname);
                } catch (NumberFormatException e) {
                    System.err.println("❌ userId가 Long 타입이 아님: " + userIdStr);
                }
            }
        }

        return null;
    }
}
