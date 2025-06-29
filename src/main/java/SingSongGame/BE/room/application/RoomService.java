package SingSongGame.BE.room.application;

import SingSongGame.BE.room.application.converter.RoomRequestConverter;
import SingSongGame.BE.room.application.converter.RoomResponseConverter;
import SingSongGame.BE.room.application.dto.request.CreateRoomRequest;
import SingSongGame.BE.room.application.dto.response.CreateRoomResponse;
import SingSongGame.BE.room.application.dto.response.GetRoomResponse;
import SingSongGame.BE.room.persistence.GameStatus;
import SingSongGame.BE.room.persistence.Room;
import SingSongGame.BE.room.persistence.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomRequestConverter requestConverter;
    private final RoomResponseConverter responseConverter;
    //private final AuthRepository authRepository;

    @Transactional
    public CreateRoomResponse createRoom(CreateRoomRequest request) {
        //User host = authRepository.findById(request.getHostId())
        //        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        Room room = requestConverter.toEntity(request);
        Long saveId = roomRepository.save(room).getId();
        return responseConverter.from(saveId);
    }

    // 로비 내부 방들을 조회할 때 비밀번호를 함께 넘겨줘서 프론트한테 각 방의 비밀번호를 가지고 있는게 맞을까?
    // 아니면 유저가 특정 방을 접속할 때 비밀번호를 조회하는게 맞을까?
    public List<GetRoomResponse> getRoomsInRoby() {
        List<Room> rooms = roomRepository.findAll()
                                         .stream()
                                         .filter(type -> type.getGameStatus() != GameStatus.DELETED)
                                         .collect(Collectors.toList());
        List<GetRoomResponse> response = responseConverter.from(rooms);
        return response;
    }
}
