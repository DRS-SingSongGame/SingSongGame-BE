package SingSongGame.BE.song.application;

import SingSongGame.BE.song.application.dto.response.SongCSV;
import SingSongGame.BE.song.persistence.Song;
import SingSongGame.BE.song.persistence.SongRepository;
import SingSongGame.BE.song.persistence.Tag;
import SingSongGame.BE.song.persistence.TagRepository;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class SongCsvLoader implements CommandLineRunner {

    private final SongRepository songRepository;
    private final TagRepository tagRepository;

    @Override
    public void run(String... args) throws Exception {
        Reader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("dataset.csv").getInputStream(),
                StandardCharsets.UTF_8
        ));

        CsvToBean<SongCSV> csvToBean = new CsvToBeanBuilder<SongCSV>(reader)
                .withType(SongCSV.class)
                .withIgnoreLeadingWhiteSpace(true)
                .build();

        // 전체 데이터 사용
        List<SongCSV> songs = csvToBean.parse();

        for (SongCSV dto : songs) {
            String tagsRaw = Optional.ofNullable(dto.getTags()).orElse("");
            List<Tag> tagList = Arrays.stream(tagsRaw.split(","))
                    .map(String::trim)
                    .filter(t -> !t.isBlank())
                    .distinct()  // 태그 이름에서 먼저 중복 제거
                    .map(tagName ->
                            tagRepository.findByName(tagName)
                                    .orElseGet(() -> tagRepository.save(new Tag(tagName)))
                    )
                    .collect(Collectors.toList());

            Song song = Song.builder()
                    .title(dto.getTitle())
                    .artist(dto.getArtist())
                    .audioUrl(dto.getAudioUrl())
                    .lyrics(dto.getLyrics())
                    .tags(tagList)
                    .hint(dto.getHint())
                    .answer(dto.getAnswer())
                    .build();

            songRepository.save(song);
        }

        System.out.println("✅ 총 " + songs.size() + "개의 노래를 저장했습니다.");
    }
}

