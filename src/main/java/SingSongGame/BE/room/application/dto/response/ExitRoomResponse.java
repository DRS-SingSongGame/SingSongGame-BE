package SingSongGame.BE.room.application.dto.response;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.room.persistence.GameStatus;
import SingSongGame.BE.room.persistence.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExitRoomResponse {
    private GameStatus gameStatus;
    private Long currentPlayer;
    private List<User> users;
    private String hostName;

}
