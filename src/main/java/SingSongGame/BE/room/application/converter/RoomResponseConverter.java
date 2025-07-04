package SingSongGame.BE.room.application.converter;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.in_game.persistence.InGame;
import SingSongGame.BE.in_game.persistence.InGameRepository;
import SingSongGame.BE.room.application.dto.response.CreateRoomResponse;
import SingSongGame.BE.room.application.dto.response.GetRoomResponse;
import SingSongGame.BE.room.application.dto.response.PlayerInfo;
import SingSongGame.BE.room.persistence.Room;
import SingSongGame.BE.room.persistence.GameStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoomResponseConverter {

    private final InGameRepository inGameRepository;

    public CreateRoomResponse from(Long roomId) {
        return CreateRoomResponse.builder()
                .id(roomId)
                .build();
    }

    public GetRoomResponse from(Room room, GameStatus gameStatus) {
        List<PlayerInfo> players = inGameRepository.findAllByRoom(room).stream()
                .map(inGame -> PlayerInfo.builder()
                        .id(inGame.getUser().getId())
                        .nickname(inGame.getUser().getName())
                        .avatar(inGame.getUser().getImageUrl())
                        .build())
                .collect(Collectors.toList());

        return GetRoomResponse.builder()
                .roomId(room.getId())
                .roomName(room.getName())
                .roomType(room.getRoomType())
                .isPrivate(room.getIsPrivate())
                .maxPlayer(room.getMaxPlayer())
                .gameStatus(gameStatus)
                .hostId(room.getHost().getId())
                .hostName(room.getHost().getName())
                .players(players)
                .build();
    }

    public List<GetRoomResponse> from(List<Room> rooms) {
        // 이 메서드는 RoomService에서 GameSession을 조회하여 GameStatus를 넘겨주도록 변경해야 함
        throw new UnsupportedOperationException("이 메서드는 GameStatus를 인자로 받도록 변경되어야 합니다.");
    }

}
