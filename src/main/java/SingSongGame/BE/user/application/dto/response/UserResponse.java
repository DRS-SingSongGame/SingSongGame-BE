package SingSongGame.BE.user.application.dto.response;

import SingSongGame.BE.auth.persistence.User;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        String profileImage
) {
    public UserResponse(User user) {
        this(user.getId(), user.getEmail(), user.getName(), user.getImageUrl());
    }
}
