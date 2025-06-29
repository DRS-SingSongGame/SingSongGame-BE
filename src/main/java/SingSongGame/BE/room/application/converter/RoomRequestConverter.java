package SingSongGame.BE.room.application.converter;

import SingSongGame.BE.room.application.dto.request.CreateRoomRequest;
import SingSongGame.BE.room.persistence.GameStatus;
import SingSongGame.BE.room.persistence.Room;
import org.springframework.stereotype.Component;

@Component
public class RoomRequestConverter {

    public Room toEntity(CreateRoomRequest request) {
        return Room.builder()
                   .name(request.getName())
                   .room(request.getRoomType())
                   .isPrivate(request.getIsPrivate())
                   .password(request.getRoomPassword())
                   .maxPlayer(request.getMaxPlayer())
                   .gameStatus(GameStatus.valueOf("WAITING"))
                   //.host(host) // 소셜 로그인 구현 후 수정 필요.
                   .build();
    }
}
