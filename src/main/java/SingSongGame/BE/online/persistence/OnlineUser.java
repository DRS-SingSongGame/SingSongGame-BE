package SingSongGame.BE.online.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OnlineUser {

    private Long userId;

    private String username;

    private String imageUrl;

    private OnlineLocation location;
}
