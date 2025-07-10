package SingSongGame.BE.song.application;

import SingSongGame.BE.song.application.dto.request.SongVerifyRequest;
import SingSongGame.BE.song.application.dto.response.SongResponse;
import SingSongGame.BE.song.application.dto.response.SongVerifyResponse;
import SingSongGame.BE.song.persistence.Song;
import SingSongGame.BE.song.persistence.SongRepository;
import SingSongGame.BE.song.persistence.Tag;
import SingSongGame.BE.song.persistence.TagRepository;
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
    private final TagRepository tagRepository;

    @Transactional
    public Song getRandomSong() {
        return getRandomSong(Collections.emptySet());
    }

    @Transactional
    public Song getRandomSong(Set<Long> usedSongIds) {
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

        return candidates.get(new Random().nextInt(candidates.size()));
    }

    @Transactional(readOnly = true)
    public Song getRandomSongByTagNames(Set<String> keywordNames, Set<Long> usedSongIds) {
        // ✅ 전체 선택이거나 아무 태그 없음 → 전체 랜덤
        if (keywordNames == null || keywordNames.isEmpty() || keywordNames.contains("전체")) {
            return getRandomSong(usedSongIds);
        }

        List<Tag> tags = tagRepository.findByNameIn(keywordNames);
        List<Long> tagIds = tags.stream().map(Tag::getId).toList();

        List<Song> candidates = songRepository.findSongsByTagIds(tagIds)
                .stream()
                .filter(song -> !usedSongIds.contains(song.getId()))
                .toList();

        if (candidates.isEmpty()) {
            throw new IllegalStateException("출제 가능한 노래가 없습니다.");
        }

        return candidates.get(new Random().nextInt(candidates.size()));
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
