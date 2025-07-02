package SingSongGame.BE.room.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JoinRoomRequest {
    //private Long roomId;
    private Integer password; // 비밀번호가 있는 방의 경우
} 