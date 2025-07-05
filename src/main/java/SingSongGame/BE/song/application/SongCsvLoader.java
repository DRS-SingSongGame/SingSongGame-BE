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

        // 전체 파싱 후 랜덤 50개 선택
        List<SongCSV> songs = csvToBean.parse();
        Collections.shuffle(songs);
        List<SongCSV> randomSongs = songs.stream().limit(50).toList();

        for (SongCSV dto : randomSongs) {
            String tagsRaw = Optional.ofNullable(dto.getTags()).orElse("");
            List<Tag> tagList = Arrays.stream(tagsRaw.split(","))
                    .map(String::trim)
                    .filter(t -> !t.isBlank())
                    .map(tagName ->
                            tagRepository.findByName(tagName)
                                    .orElseGet(() -> tagRepository.save(new Tag(tagName)))
                    )
                    .distinct()
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

        System.out.println("✅ 랜덤으로 " + randomSongs.size() + "개의 노래를 저장했습니다.");
    }
}

