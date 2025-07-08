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

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;

    @Transactional
    public SongResponse getRandomSong() {
        return getRandomSong(Collections.emptySet());
    }

    @Transactional
    public SongResponse getRandomSong(Set<Long> usedSongIds) {
        List<Song> candidates;

        if (usedSongIds.isEmpty()) {
            // 아무 것도 제외할 게 없으면 전체 목록 사용
            candidates = songRepository.findAll();
        } else {
            candidates = songRepository.findAllExcluding(usedSongIds);
        }

        if (candidates.isEmpty()) {
            return null; // 더 이상 출제할 노래가 없음
        }

        Song randomSong = candidates.get(new Random().nextInt(candidates.size()));

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
                randomSong.getLyrics(),
                null // 라운드 정보는 InGameService에서 설정
        );
    }

    public SongVerifyResponse verifyAnswer(SongVerifyRequest request) {
        Song song = songRepository.findById(request.songId())
                .orElseThrow(() -> new IllegalArgumentException("해당 곡이 존재하지 않습니다."));

        String correctAnswer = normalize(song.getAnswer());
        String userInput = normalize(request.userAnswer());

        boolean isCorrect = correctAnswer.equals(userInput);

        return new SongVerifyResponse(isCorrect, song.getTitle());
    }

    private String normalize(String input) {
        return input
                .toLowerCase()          // 대소문자 무시
                .replaceAll("[^a-z0-9가-힣]", "");
    }
}
