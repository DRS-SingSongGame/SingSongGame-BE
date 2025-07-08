package SingSongGame.BE.ai_game.dto.response;

public record AiGameStartCountdownResponse(
        String message,
        int countdownSeconds
) {
}
