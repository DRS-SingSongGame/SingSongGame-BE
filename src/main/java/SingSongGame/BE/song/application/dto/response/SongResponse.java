package SingSongGame.BE.song.application.dto.response;

import SingSongGame.BE.song.persistence.Song;
import SingSongGame.BE.song.persistence.Tag;

import java.util.List;
import java.util.stream.Collectors;

public record SongResponse(
        Long id,
        String title,
        String artist,
        String audioUrl,
        List<String> tags,
        String hint,
        String lyrics,
        Integer round,
        Integer maxRound
) {
    public Song toSongEntity() {
        return Song.builder()
                .id(this.id)
                .title(this.title)
                .artist(this.artist)
                .audioUrl(this.audioUrl)
                .hint(this.hint)
                .tags(this.tags != null ? this.tags.stream().map(Tag::new).collect(Collectors.toList()) : null) // Tag 생성자에 String을 받는 생성자 필요
                .build();
    }

    public static SongResponse from(Song song, Integer round, Integer maxRound) {
        return new SongResponse(
                song.getId(),
                song.getTitle(),
                song.getArtist(),
                song.getAudioUrl(),
                song.getTags().stream().map(Tag::getName).collect(Collectors.toList()),
                song.getHint(),
                song.getLyrics(),
                round,
                maxRound
        );
    }
}