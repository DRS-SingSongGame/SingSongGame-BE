package SingSongGame.BE.in_game.persistence;

import SingSongGame.BE.room.persistence.Room;
import SingSongGame.BE.room.persistence.GameStatus;
import SingSongGame.BE.song.persistence.Song;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GameSession {

    @Id
    private Long id; // Room의 ID를 공유

    @OneToOne
    @MapsId // Room의 ID를 이 엔티티의 ID로 사용
    @JoinColumn(name = "room_id")
    private Room room;

    @Enumerated(EnumType.STRING)
    private GameStatus gameStatus;

    private Integer currentRound;

    @Column(nullable = false)
    private int maxRound;


    @ManyToOne
    @JoinColumn(name = "current_song_id")
    private Song currentSong;

    private LocalDateTime roundStartTime;
    private Integer roundDuration; // 초
    private boolean roundAnswered; // 현재 라운드 정답 여부


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_session_player_scores", joinColumns = @JoinColumn(name = "game_session_id"))
    @MapKeyColumn(name = "user_id")
    @Column(name = "score")
    @Builder.Default
    private Map<Long, Integer> playerScores = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private Set<Long> usedSongIds = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_session_keywords", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "keyword")
    private Set<String> keywords;



    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void setRoundAnswered(boolean roundAnswered) {
        this.roundAnswered = roundAnswered;
        this.updatedAt = LocalDateTime.now();
    }


    public void updateGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRoundInfo(Integer currentRound, Song currentSong, LocalDateTime roundStartTime) {
        this.currentRound = currentRound;
        this.currentSong = currentSong;
        this.roundStartTime = roundStartTime;
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePlayerScore(Long userId, Integer score) {
        this.playerScores.put(userId, score);
        this.updatedAt = LocalDateTime.now();
    }

    public void resetForNewGame() {
        this.currentRound = 0;
        this.currentSong = null;
        this.roundStartTime = null;
        this.roundAnswered = false;
        this.playerScores.clear();
        this.usedSongIds.clear();
        this.gameStatus = GameStatus.WAITING; // 또는 READY, 룸 상태와 맞춰서
        this.updatedAt = LocalDateTime.now();
//        if (this.keywords != null) {
//            this.keywords.clear();
//        }
    }
}
