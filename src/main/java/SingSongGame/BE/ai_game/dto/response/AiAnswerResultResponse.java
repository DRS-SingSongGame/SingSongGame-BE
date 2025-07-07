package SingSongGame.BE.ai_game.dto.response;

public record AiAnswerResultResponse(
        String username,
        boolean correct,
        String correctAnswer
) {}
