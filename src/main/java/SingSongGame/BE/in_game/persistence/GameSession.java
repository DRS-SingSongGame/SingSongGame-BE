package SingSongGame.BE.in_game.persistence;

import SingSongGame.BE.room.persistence.Room;
import SingSongGame.BE.room.persistence.GameStatus;
import SingSongGame.BE.song.persistence.Song;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
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

    @ManyToOne
    @JoinColumn(name = "current_song_id")
    private Song currentSong;

    private LocalDateTime roundStartTime;
    private Integer roundDuration; // 초
    private boolean roundAnswered; // 현재 라운드 정답 여부


    @ElementCollection
    @CollectionTable(name = "game_session_player_scores", joinColumns = @JoinColumn(name = "game_session_id"))
    @MapKeyColumn(name = "user_id")
    @Column(name = "score")
    @Builder.Default
    private Map<Long, Integer> playerScores = new HashMap<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
}
