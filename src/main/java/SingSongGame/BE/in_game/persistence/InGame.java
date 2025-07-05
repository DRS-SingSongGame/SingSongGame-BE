package SingSongGame.BE.in_game.persistence;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.room.persistence.Room;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class InGame {

    @Id @GeneratedValue
    @Column(nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Integer score;

    private Boolean ready;

    public void updateScore(int newScore) {
        this.score = newScore;
    }

    public void updateReady(Boolean curStatus) {
        this.ready = curStatus;
    }
}
