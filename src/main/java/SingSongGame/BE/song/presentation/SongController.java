package SingSongGame.BE.song.presentation;

import SingSongGame.BE.song.application.SongService;
import SingSongGame.BE.song.application.dto.response.SongResponse;
import SingSongGame.BE.song.persistence.Song;
import SingSongGame.BE.song.persistence.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import SingSongGame.BE.song.persistence.Tag;

import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/song")
@RequiredArgsConstructor
public class SongController {

    private final SongRepository songRepository;

    @GetMapping("/random")
    public ResponseEntity<SongResponse> getRandomSong() {
        List<Song> allSongs = songRepository.findAll();
        if (allSongs.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        Song randomSong = allSongs.get(new Random().nextInt(allSongs.size()));

        // Tag → String 리스트 변환
        List<String> tagNames = randomSong.getTags().stream()
                .map(Tag::getName)
                .toList();

        return ResponseEntity.ok(new SongResponse(
                randomSong.getId(),
                randomSong.getTitle(),
                randomSong.getArtist(),
                randomSong.getAudioUrl(),
                tagNames,
                randomSong.getHint()
        ));
    }
}
