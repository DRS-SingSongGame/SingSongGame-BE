package SingSongGame.BE.song.presentation;

import SingSongGame.BE.song.application.SongService;
import SingSongGame.BE.song.application.dto.request.SongVerifyRequest;
import SingSongGame.BE.song.application.dto.response.SongResponse;
import SingSongGame.BE.song.application.dto.response.SongVerifyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/song")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    @GetMapping("/random")
    public ResponseEntity<SongResponse> getRandomSong() {
        SongResponse songResponse = songService.getRandomSong();
        if (songResponse == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(songResponse);
    }

    @PostMapping("/verify")
    public ResponseEntity<SongVerifyResponse> verifyAnswer(@RequestBody SongVerifyRequest request) {
        return ResponseEntity.ok(songService.verifyAnswer(request));
    }
}
