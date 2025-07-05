package SingSongGame.BE.song.application.dto.request;

public record SongVerifyRequest(
        Long songId,
        String userAnswer
) {
}
