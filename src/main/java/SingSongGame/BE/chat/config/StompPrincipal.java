package SingSongGame.BE.chat.config;

import java.security.Principal;

public class StompPrincipal implements Principal {

    private final Long userId;
    private final String nickname;

    public StompPrincipal(Long userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
    }

    @Override
    public String getName() {
        return userId.toString(); // 또는 userId.toString() 도 가능
    }

    public Long getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }
}
