package SingSongGame.BE.room_keyword.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class KeywordConfirmRequest {
    private Long roomId;
    private List<Long> keywords;
}
