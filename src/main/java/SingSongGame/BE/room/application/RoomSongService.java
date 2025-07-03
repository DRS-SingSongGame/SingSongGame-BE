package SingSongGame.BE.room.application;

import SingSongGame.BE.song.persistence.Song;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoomSongService {
    // roomId → 현재 Song
    private final Map<Long, Song> currentSongs = new ConcurrentHashMap<>();

    public void setCurrentSongForRoom(Long roomId, Song song) {
        currentSongs.put(roomId, song);
    }

    public Song getCurrentSongForRoom(Long roomId) {
        return currentSongs.get(roomId);
    }

    public void clearRoom(Long roomId) {
        currentSongs.remove(roomId);
    }
}
