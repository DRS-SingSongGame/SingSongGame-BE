package SingSongGame.BE.song.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Tag {      // Song에 들어갈 Tag. Song과 대다대 관계를 가짐

    @Id
    @GeneratedValue
    private Long id;

    private String name;
}
