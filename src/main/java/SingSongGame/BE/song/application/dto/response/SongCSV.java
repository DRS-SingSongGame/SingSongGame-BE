package SingSongGame.BE.song.application.dto.response;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SongCSV {
    @CsvBindByName(column = "title")
    private String title;

    @CsvBindByName(column = "artist")
    private String artist;

    @CsvBindByName(column = "audio_url")
    private String audioUrl;

    @CsvBindByName(column = "lyrics")
    private String lyrics;

    @CsvBindByName(column = "tags")
    private String tags;   // CSV에선 문자열로 들어있음 → 후에 분리

    @CsvBindByName(column = "hint")
    private String hint;

    @CsvBindByName(column = "answer")
    private String answer;
}
