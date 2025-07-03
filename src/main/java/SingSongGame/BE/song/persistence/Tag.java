package SingSongGame.BE.song.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Tag {      // Song에 들어갈 Tag. Song과 대다대 관계를 가짐

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    public Tag(String name) {
        this.name = name;
    }
}
