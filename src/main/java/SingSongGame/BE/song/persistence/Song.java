package SingSongGame.BE.song.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;             // 예: "Attention"
    private String artist;            // 예: "NewJeans"

    @Column(name = "audio_url", length = 1000)
    private String audioUrl;          // S3 URL

    @Column(columnDefinition = "TEXT")
    private String lyrics;            // 전체 가사

    @ManyToMany
    @JoinTable(
            name = "song_tag",
            joinColumns = @JoinColumn(name = "song_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();              // "2023,댄스,여자 가수,여자 아이돌"

    @Column(length = 255)
    private String hint;              // 예: "a_______"

    @Column(length = 255)
    private String answer;            // 예: "attention"
}
