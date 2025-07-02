package SingSongGame.BE.room.persistence;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.in_game.persistence.InGame;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Room {

    @Id @GeneratedValue
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private RoomType room;


    private Boolean isPrivate;
    private Integer password;
    private Integer maxPlayer;

    @Enumerated(EnumType.STRING)
    private GameStatus gameStatus;

    @ManyToOne
    @JoinColumn(name = "host_id")
    private User host;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "room")
    private List<InGame> inGames = new ArrayList<>();

    public void updateGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
        this.updatedAt = LocalDateTime.now();
    }
}
