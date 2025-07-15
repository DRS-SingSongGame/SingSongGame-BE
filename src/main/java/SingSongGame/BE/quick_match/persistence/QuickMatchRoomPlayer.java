package SingSongGame.BE.quick_match.persistence;

import SingSongGame.BE.auth.persistence.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuickMatchRoomPlayer {
    @Id
    @GeneratedValue
    private Long id;

    private int mmrAtMatchTime;

    private int score;

    public QuickMatchRoomPlayer(User user, int score) {
        this.user = user;
        this.score = score;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private QuickMatchRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Long getPlayerId() {
        return user != null ? user.getId() : null;
    }
}
