package SingSongGame.BE.room.application.dto.response;

import SingSongGame.BE.auth.persistence.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinRoomResponse {
    private Long roomId;
    private String roomName;
    private String hostName;
    private List<User> users; // 방 내부에 존재하는 유저들 (호스트 포함)
    private Integer currentPlayerCount;
    private Integer maxPlayer;
    private String gameStatus; // 진행중 대기중 삭제됨 꽉찬방
} 