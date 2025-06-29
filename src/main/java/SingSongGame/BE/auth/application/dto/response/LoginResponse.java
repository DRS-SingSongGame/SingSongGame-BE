package SingSongGame.BE.auth.application.dto.response;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        boolean isFirstLogin,
        Long userId,
        String email
)
{}
