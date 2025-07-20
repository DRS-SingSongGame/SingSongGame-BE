package SingSongGame.BE.in_game.persistence;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.room.persistence.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InGameRepository extends JpaRepository<InGame, Long> {
    
    // 특정 방의 인원 수 조회
    long countByRoom(Room room);
    
    // 특정 방에 특정 사용자가 있는지 확인
    boolean existsByRoomAndUser(Room room, User user);
    
    // 특정 방의 특정 사용자 조회
    Optional<InGame> findByRoomAndUser(Room room, User user);

    Optional<InGame> findByUserAndRoom(User user, Room room);
    
    // 특정 방의 모든 참가자 조회
    @Query("SELECT ig FROM InGame ig WHERE ig.room = :room")
    List<InGame> findAllByRoom(@Param("room") Room room);

    List<InGame> findByRoomId(Long roomId);

    List<InGame> findAllByUser(User user);

    List<InGame> findAllByUserId(Long userId);

    void deleteAllByRoom(Room room);
}
