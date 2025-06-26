package SingSongGame.BE.in_game.persistence;

import SingSongGame.BE.in_game.persistence.InGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InGameRepository extends JpaRepository<InGame, Long> {
}
