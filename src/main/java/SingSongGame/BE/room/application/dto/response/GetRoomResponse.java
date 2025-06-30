package SingSongGame.BE.room.application.dto.response;

import SingSongGame.BE.room.persistence.GameStatus;
import SingSongGame.BE.room.persistence.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetRoomResponse {
    private Long roomId;
    private String roomName;
    private RoomType roomType;
    private Boolean isPrivate;
    private Integer maxPlayer;
    private GameStatus gameStatus;
    private Long hostId;
    private String hostName;
}
