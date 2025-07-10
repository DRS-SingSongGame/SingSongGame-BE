package SingSongGame.BE.online.application.dto.response;

import SingSongGame.BE.online.persistence.OnlineLocation;

public record OnlineUserResponse(
        Long userId,
        String username,
        String imageUrl,
        OnlineLocation location
) {
}
