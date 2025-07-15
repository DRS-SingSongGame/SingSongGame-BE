package SingSongGame.BE.quick_match.persistence;

import SingSongGame.BE.room.persistence.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuickMatchRepository extends JpaRepository<QuickMatchRoom, Long> {
    Optional<QuickMatchRoom> findByRoomCode(String roomCode);
    Optional<QuickMatchRoom> findByRoom(Room room);
}
