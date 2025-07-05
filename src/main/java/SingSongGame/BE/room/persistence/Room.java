package SingSongGame.BE.room.persistence;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.in_game.persistence.GameSession;
import SingSongGame.BE.in_game.persistence.InGame;
import jakarta.persistence.*;
import lombok.*;

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
    private RoomType roomType;

    private Boolean isPrivate;
    private Integer password;
    private Integer maxPlayer;

    @ManyToOne
    @JoinColumn(name = "host_id")
    private User host;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "room")
    private List<InGame> inGames = new ArrayList<>();

    @Setter
    @OneToOne(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private GameSession gameSession;

    public Room(Long id) {
        this.id = id;
    }



    public void changeHost(User newHost) {
        this.host = newHost;
    }
}
