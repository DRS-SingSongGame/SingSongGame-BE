package SingSongGame.BE.auth.persistence;

import SingSongGame.BE.in_game.persistence.InGame;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class User {

    @Id @GeneratedValue
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    //@Column(nullable = false)
    private String imageUrl;

    @OneToMany(mappedBy = "user")
    private List<InGame> inGames = new ArrayList<>();
}
