package SingSongGame.BE.room.application.dto.request;

import SingSongGame.BE.room.persistence.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateRoomRequest {
    private String name;
    private RoomType roomType;
    private Boolean isPrivate;
    private Integer roomPassword;
    private Integer currentPlayer;
    private Integer maxPlayer;
    //private String gameStatus;
    //    private Long hostId;
}
