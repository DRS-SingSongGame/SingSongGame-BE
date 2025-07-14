package SingSongGame.BE.song.application.dto.response;

import SingSongGame.BE.song.persistence.Song;
import SingSongGame.BE.song.persistence.Tag;

import java.util.Collections;
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
        Integer maxRound,
        Long serverStartTime
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
        List<String> tagNames = Collections.emptyList();

        // ✅ 안전한 태그 접근
        try {
            if (song.getTags() != null) {
                tagNames = song.getTags().stream()
                        .map(Tag::getName)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            // 태그 로딩 실패 시 빈 리스트
            System.out.println("태그 로딩 실패: " + e.getMessage());
        }

        return new SongResponse(
                song.getId(),
                song.getTitle(),
                song.getArtist(),
                song.getAudioUrl(),
                tagNames, // ✅ 안전하게 처리된 태그 리스트
                song.getHint(),
                song.getLyrics(),
                round,
                maxRound,
                System.currentTimeMillis()
        );
    }
}