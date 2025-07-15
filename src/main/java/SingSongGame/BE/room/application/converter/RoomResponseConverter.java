package SingSongGame.BE.room.application.converter;

import SingSongGame.BE.in_game.persistence.InGameRepository;
import SingSongGame.BE.room.application.dto.response.CreateRoomResponse;
import SingSongGame.BE.room.application.dto.response.GetRoomResponse;
import SingSongGame.BE.room.application.dto.response.PlayerInfo;
import SingSongGame.BE.room.persistence.GameStatus;
import SingSongGame.BE.room.persistence.Room;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomResponseConverter {

    private final InGameRepository inGameRepository;

    public CreateRoomResponse from(Room room) {
        return CreateRoomResponse.builder()
                .id(room.getId())
                .maxRound(room.getMaxRound()) // ✅ 추가된 필드
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

        // 🔑 QuickMatchRoom이 연결되어 있다면 roomCode를 가져온다
        String roomCode = null;
        if (room.getQuickMatchRoom() != null) {
            roomCode = room.getQuickMatchRoom().getRoomCode();
        }

        return GetRoomResponse.builder()
                .roomId(room.getId())
                .roomName(room.getName())
                .roomCode(roomCode) // ✅ 여기만 수정됨!
                .roomType(room.getRoomType())
                .isPrivate(room.getIsPrivate())
                .maxPlayer(room.getMaxPlayer())
                .maxRound(room.getMaxRound())
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
