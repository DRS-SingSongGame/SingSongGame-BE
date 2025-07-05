package SingSongGame.BE.room.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserReadyStatusRequest {
    private Boolean userReadyStatus;
}
