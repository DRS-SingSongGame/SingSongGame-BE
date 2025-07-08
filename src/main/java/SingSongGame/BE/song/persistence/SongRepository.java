package SingSongGame.BE.song.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface SongRepository extends JpaRepository<Song, Long> {
    @Query("SELECT s FROM Song s WHERE s.id NOT IN :usedIds")
    List<Song> findAllExcluding(@Param("usedIds") Set<Long> usedIds);
    @Query("SELECT DISTINCT s FROM Song s JOIN FETCH s.tags t WHERE t.id IN :tagIds")
    List<Song> findSongsByTagIds(@Param("tagIds") List<Long> tagIds);
}
