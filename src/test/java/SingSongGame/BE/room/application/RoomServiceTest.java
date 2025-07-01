package SingSongGame.BE.room.application;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.room.application.dto.request.CreateRoomRequest;
import SingSongGame.BE.room.application.dto.response.CreateRoomResponse;
import SingSongGame.BE.room.persistence.GameStatus;
import SingSongGame.BE.room.persistence.Room;
import SingSongGame.BE.room.persistence.RoomRepository;
import SingSongGame.BE.room.persistence.RoomType;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RoomServiceTest {

    @Autowired
    RoomService roomService;

    @Autowired
    RoomRepository roomRepository;

    @Test
    void createRoomTest() {

        //given
        CreateRoomRequest room = CreateRoomRequest.builder()
                                     .name("tony")
                                     .roomType(RoomType.valueOf("KEY_SING_YOU"))
                                     .isPrivate(true)
                                     .roomPassword(1234)
                                     .maxPlayer(4)
                                     //.host(host) // 소셜 로그인 구현 후 수정 필요.
                                     .build();

        User dummyUser = User.builder()
                             .id(System.currentTimeMillis())
                             .name("사용자_" + (System.currentTimeMillis() % 1000))
                             .build();


        //when
        CreateRoomResponse roomId = roomService.createRoom(room, dummyUser);
        Room roomTest = roomRepository.findById(roomId.getId()).orElseThrow();

        //then
        assertThat(roomTest.getName()).isEqualTo("tony");
        assertThat(roomTest.getRoom()).isEqualTo(RoomType.KEY_SING_YOU);
        assertThat(roomTest.getIsPrivate()).isTrue();
        assertThat(roomTest.getPassword()).isEqualTo(1234);
        assertThat(roomTest.getMaxPlayer()).isEqualTo(4);
        assertThat(roomTest.getGameStatus().name()).isEqualTo("WAITING");
    }
}