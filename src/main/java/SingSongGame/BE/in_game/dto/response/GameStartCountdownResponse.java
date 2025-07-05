package SingSongGame.BE.in_game.dto.response;

public record GameStartCountdownResponse(
    String message,
    int countdownSeconds
) {}