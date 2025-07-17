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
        return getRandomSong(Collections.emptySet(), null);
    }

    @Transactional
    public Song getRandomSong(Set<Long> usedSongIds) {
        return getRandomSong(usedSongIds, null);
    }

    @Transactional
    public Song getRandomSong(Set<Long> usedSongIds, String excludeArtist) {
        // 모든 조건을 한 번에 처리하는 Repository 메서드 사용
        List<Song> candidates = songRepository.findRandomCandidates(usedSongIds, excludeArtist);

        if (candidates.isEmpty()) {
            return null; // 더 이상 출제할 노래가 없음
        }

        return candidates.get(new Random().nextInt(candidates.size()));
    }

    @Transactional(readOnly = true)
    public Song getRandomSongByTagNames(Set<String> keywordNames, Set<Long> usedSongIds, String excludeArtist) {
        // ✅ 전체 선택이거나 아무 태그 없음 → 전체 랜덤 (tags와 함께 조회)
        System.out.println("🎵 검색할 키워드들: " + keywordNames);
        System.out.println("🎵 제외할 가수: " + excludeArtist);

        if (keywordNames == null || keywordNames.isEmpty() || keywordNames.contains("전체")) {
            System.out.println("🎵 전체 랜덤 선택됨");
            List<Song> allSongs = songRepository.findAllWithTagsExcluding(usedSongIds);
            return selectSongExcludingArtist(allSongs, excludeArtist);
        }

        System.out.println("🎵 키워드 기반 검색");
        List<Tag> tags = tagRepository.findByNameIn(keywordNames);
        List<Long> tagIds = tags.stream().map(Tag::getId).toList();

        List<Song> candidates = songRepository.findSongsByTagIds(tagIds)
                .stream()
                .filter(song -> !usedSongIds.contains(song.getId()))
                .toList();

        return selectSongExcludingArtist(candidates, excludeArtist);
    }

    private Song selectSongExcludingArtist(List<Song> candidates, String excludeArtist) {
        if (candidates.isEmpty()) {
            throw new IllegalStateException("출제 가능한 노래가 없습니다.");
        }

        // 이전 가수와 다른 노래만 필터링
        List<Song> filteredCandidates = candidates;

        if (excludeArtist != null && !excludeArtist.isBlank()) {
            filteredCandidates = candidates.stream()
                    .filter(song -> !excludeArtist.equals(song.getArtist()))
                    .toList();
        }

        // 필터링된 후보가 없으면 전체 후보 사용
        if (filteredCandidates.isEmpty()) {
            System.out.println("⚠️ 이전 가수 제외 후 후보가 없음. 전체 후보 사용");
            filteredCandidates = candidates;
        }

        return filteredCandidates.get(new Random().nextInt(filteredCandidates.size()));
    }

    @Transactional(readOnly = true)
    public SongResponse createSongResponse(Song song, Integer round, Integer maxRound) {
        // ✅ 이미 선택된 song 객체로 DTO 변환만
        return SongResponse.from(song, round, maxRound);
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
