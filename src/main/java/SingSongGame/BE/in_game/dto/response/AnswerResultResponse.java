package SingSongGame.BE.in_game.dto.response;

public record AnswerResultResponse(
        String username,
        boolean correct,
        String correctAnswer
) {}