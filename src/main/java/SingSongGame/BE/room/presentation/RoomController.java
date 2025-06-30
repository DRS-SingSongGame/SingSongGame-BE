package SingSongGame.BE.room.presentation;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.common.annotation.LoginUser;
import SingSongGame.BE.common.response.ApiResponse;
import SingSongGame.BE.common.response.ApiResponseBody;
import SingSongGame.BE.common.response.ApiResponseGenerator;
import SingSongGame.BE.common.response.MessageCode;
import SingSongGame.BE.room.application.RoomService;
import SingSongGame.BE.room.application.dto.request.CreateRoomRequest;
import SingSongGame.BE.room.application.dto.response.CreateRoomResponse;
import SingSongGame.BE.room.application.dto.response.GetRoomResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @Operation(summary = "방 생성")
    @PostMapping
    public ApiResponse<ApiResponseBody.SuccessBody<CreateRoomResponse>> createRoom(
            @RequestBody CreateRoomRequest request,
            @LoginUser User loginUser) {
        CreateRoomResponse response = roomService.createRoom(request, loginUser);
        return ApiResponseGenerator.success(response, HttpStatus.CREATED, MessageCode.CREATE);
    }

    @Operation(summary = "게임 방 목록 조회")
    @GetMapping()
    public ApiResponse<ApiResponseBody.SuccessBody<List<GetRoomResponse>>> getRooms() {
        List<GetRoomResponse> response = roomService.getRoomsInRoby();
        return ApiResponseGenerator.success(response, HttpStatus.OK, MessageCode.GET);
    }

    @Operation(summary = "방 수정")
    @PutMapping("/{roomId}")
    public ApiResponse<ApiResponseBody.SuccessBody<Void>> updateRoom() {
        // 구현 필요
        return ApiResponseGenerator.success(HttpStatus.OK, MessageCode.UPDATE);
    }

    @Operation(summary = "방 참여")
    @PostMapping("/{roomId}/enter")
    public ApiResponse<ApiResponseBody.SuccessBody<Void>> enterRoom() {
        // 구현 필요
        return ApiResponseGenerator.success(HttpStatus.OK, MessageCode.UPDATE);
    }

    @Operation(summary = "방 나가기")
    @PostMapping("/{roomId}/exit")
    public ApiResponse<ApiResponseBody.SuccessBody<Void>> exitRoom() {
        // 구현 필요
        return ApiResponseGenerator.success(HttpStatus.OK, MessageCode.UPDATE);
    }

    // 채팅 controller 구현 필요

}
