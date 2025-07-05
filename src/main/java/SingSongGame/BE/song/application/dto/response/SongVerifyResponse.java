package SingSongGame.BE.song.application.dto.response;

public record SongVerifyResponse(
        boolean correct,
        String correctTitle
) {
}
