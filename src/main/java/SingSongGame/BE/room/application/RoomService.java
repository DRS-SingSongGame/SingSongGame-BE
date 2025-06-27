package SingSongGame.BE.room.application;

import SingSongGame.BE.room.application.dto.request.CreateRoomRequest;
import SingSongGame.BE.room.persistence.GameStatus;
import SingSongGame.BE.room.persistence.Room;
import SingSongGame.BE.room.persistence.RoomRepository;
import SingSongGame.BE.auth.persistence.AuthRepository;
import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.room.persistence.RoomType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final AuthRepository authRepository;

    @Transactional
    public Long createRoom(CreateRoomRequest request) {
        //User host = authRepository.findById(request.getHostId())
        //        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        Room room = Room.builder()
                .name(request.getName())
                .room(request.getRoomType())
                .isPrivate(request.getIsPrivate())
                .password(request.getRoomPassword())
                .maxPlayer(request.getMaxPlayer())
                .gameStatus(GameStatus.valueOf("WAITING"))
                //.host(host) // 소셜 로그인 구현 후 수정 필요.
                .build();
        Long saveId = roomRepository.save(room).getId();
        return saveId;
    }
}
