package SingSongGame.BE.song.application.dto.response;

import java.util.List;

public record SongResponse(
        Long id,
        String title,
        String artist,
        String audioUrl,
        List<String> tags,
        String hint
) {}