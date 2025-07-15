package SingSongGame.BE.quick_match.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuickMatchRoomPlayerRepository extends JpaRepository<QuickMatchRoomPlayer, Long> {
    void deleteAllByRoom(QuickMatchRoom room);
}
