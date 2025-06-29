package SingSongGame.BE.auth.persistence;

import SingSongGame.BE.in_game.persistence.InGame;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User {

    @Id @GeneratedValue
    @Column(nullable = false)
    private Long id;

    @Column(nullable = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Builder.Default
    private boolean isFirstLogin = true;

    //@Column(nullable = false)
    private String imageUrl;

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<InGame> inGames = new ArrayList<>();
}
