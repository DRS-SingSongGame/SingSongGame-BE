package SingSongGame.BE.auth.application.dto.response;

public record TokenResponse(
        String accessToken,
        Long userId
) {
}
