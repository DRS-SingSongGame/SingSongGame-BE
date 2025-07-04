package SingSongGame.BE.song.application;

import SingSongGame.BE.song.application.dto.request.SongVerifyRequest;
import SingSongGame.BE.song.application.dto.response.SongResponse;
import SingSongGame.BE.song.application.dto.response.SongVerifyResponse;
import SingSongGame.BE.song.persistence.Song;
import SingSongGame.BE.song.persistence.SongRepository;
import SingSongGame.BE.song.persistence.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;

    @Transactional
    public SongResponse getRandomSong() {
        List<Song> allSongs = songRepository.findAll();
        if (allSongs.isEmpty()) {
            return null;
        }

        Song randomSong = allSongs.get(new Random().nextInt(allSongs.size()));

        List<String> tagNames = randomSong.getTags().stream()
                .map(Tag::getName)
                .toList();

        return new SongResponse(
                randomSong.getId(),
                randomSong.getTitle(),
                randomSong.getArtist(),
                randomSong.getAudioUrl(),
                tagNames,
                randomSong.getHint(),
                null // 라운드 정보는 InGameService에서 설정
        );
    }

    public SongVerifyResponse verifyAnswer(SongVerifyRequest request) {
        Song song = songRepository.findById(request.songId())
                .orElseThrow(() -> new IllegalArgumentException("해당 곡이 존재하지 않습니다."));

        String correctAnswer = song.getAnswer().trim().toLowerCase();
        String userInput = request.userAnswer().trim().toLowerCase();

        boolean isCorrect = correctAnswer.equals(userInput);

        return new SongVerifyResponse(isCorrect, song.getAnswer());
    }
}
