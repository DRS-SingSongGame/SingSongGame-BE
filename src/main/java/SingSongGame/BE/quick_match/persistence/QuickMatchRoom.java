package SingSongGame.BE.quick_match.persistence;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.room.persistence.Room;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class QuickMatchRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomCode;

    private LocalDateTime createdAt;

    private boolean gameStarted;

    private boolean gameEnded;

    private int roundCount;

    private String mode;

    @Builder.Default
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuickMatchRoomPlayer> players = new ArrayList<>();

    private int averageMmr;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "room_ref_id", foreignKey = @ForeignKey(name = "FK_quick_match_room_ref"))
    private Room room;


    public List<User> getUsers() {
        return players.stream()
                .map(QuickMatchRoomPlayer::getUser)
                .collect(Collectors.toList());
    }
}
