package SingSongGame.BE.song.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {
    @Query("SELECT s FROM Song s WHERE s.id NOT IN :usedIds")
    List<Song> findAllExcluding(@Param("usedIds") Set<Long> usedIds);
    @Query("SELECT DISTINCT s FROM Song s JOIN FETCH s.tags t WHERE t.id IN :tagIds")
    List<Song> findSongsByTagIds(@Param("tagIds") List<Long> tagIds);
    @Query("SELECT s FROM Song s JOIN FETCH s.tags WHERE s.id NOT IN :usedIds")
    List<Song> findAllWithTagsExcluding(@Param("usedIds") Set<Long> usedIds);
    @Query("SELECT s FROM Song s WHERE " +
            "(:usedSongIds IS NULL OR s.id NOT IN :usedSongIds) " +
            "AND (:excludeArtist IS NULL OR s.artist != :excludeArtist)")
    List<Song> findRandomCandidates(
            @Param("usedSongIds") Set<Long> usedSongIds,
            @Param("excludeArtist") String excludeArtist
    );
}
