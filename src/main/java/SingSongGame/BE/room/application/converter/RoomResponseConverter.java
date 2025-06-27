package SingSongGame.BE.room.application.converter;

import SingSongGame.BE.room.application.dto.response.CreateRoomResponse;
import SingSongGame.BE.room.application.dto.response.GetRoomResponse;
import SingSongGame.BE.room.persistence.Room;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoomResponseConverter {

    public CreateRoomResponse from(Long roomId) {
        return CreateRoomResponse.builder().id(roomId).build();
    }

    public GetRoomResponse from(Room room) {
        return GetRoomResponse.builder()
                .roomId(room.getId())
                .roomName(room.getName())
                .roomType(room.getRoom())
                .isPrivate(room.getIsPrivate())
                .maxPlayer(room.getMaxPlayer())
                .gameStatus(room.getGameStatus())
                .build();
    }

    public List<GetRoomResponse> from(List<Room> rooms) {
        return rooms.stream()
                .map(this::from)
                .collect(Collectors.toList());
    }

}
