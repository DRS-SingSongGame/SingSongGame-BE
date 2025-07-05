package SingSongGame.BE.room.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerInfo {
    private Long id;
    private String nickname;
    private String avatar;
    private Boolean ready;
}