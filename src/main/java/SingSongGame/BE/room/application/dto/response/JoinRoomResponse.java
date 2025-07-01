package SingSongGame.BE.room.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinRoomResponse {
    private Long roomId;
    private String roomName;
    private String hostName;
    private Integer currentPlayerCount;
    private Integer maxPlayer;
    private String gameStatus;
    private Boolean isHost;
} 