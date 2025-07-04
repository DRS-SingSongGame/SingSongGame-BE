package SingSongGame.BE.room.presentation;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.common.annotation.LoginUser;
import SingSongGame.BE.common.response.ApiResponse;
import SingSongGame.BE.common.response.ApiResponseBody;
import SingSongGame.BE.common.response.ApiResponseGenerator;
import SingSongGame.BE.common.response.MessageCode;
import SingSongGame.BE.room.application.RoomService;
import SingSongGame.BE.room.application.dto.request.CreateRoomRequest;
import SingSongGame.BE.room.application.dto.request.JoinRoomRequest;
import SingSongGame.BE.room.application.dto.response.CreateRoomResponse;
import SingSongGame.BE.room.application.dto.response.GetRoomResponse;
import SingSongGame.BE.room.application.dto.response.JoinRoomResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "특정 방 조회")
    @GetMapping("/{roomId}")
    public ApiResponse<ApiResponseBody.SuccessBody<GetRoomResponse>> getRoomById(@PathVariable Long roomId) {
        GetRoomResponse response = roomService.getRoomById(roomId); // roomService에 getRoomById 메서드가 있다고 가정
        return ApiResponseGenerator.success(response, HttpStatus.OK, MessageCode.GET);
    }

    @Operation(summary = "방 수정")
    @PutMapping("/{roomId}")
    public ApiResponse<ApiResponseBody.SuccessBody<Void>> updateRoom() {
        // 구현 필요
        return ApiResponseGenerator.success(HttpStatus.OK, MessageCode.UPDATE);
    }

    @Operation(summary = "방 참여")
    @PostMapping("/join/{roomId}")
    public ApiResponse<ApiResponseBody.SuccessBody<JoinRoomResponse>> joinRoom(
            @RequestBody JoinRoomRequest request,
            @LoginUser User user,
            @PathVariable("roomId") Long roomId) {
        
        JoinRoomResponse response = roomService.joinRoom(request, user, roomId);
        return ApiResponseGenerator.success(response, HttpStatus.OK, MessageCode.SUCCESS);
    }

    @Operation(summary = "방 나가기")
    @DeleteMapping("/{roomId}/leave")
    public ApiResponse<ApiResponseBody.SuccessBody<Void>> leaveRoom(
            @PathVariable Long roomId,
            @LoginUser User user) {
        
        roomService.leaveRoom(roomId, user);
        return ApiResponseGenerator.success(HttpStatus.OK, MessageCode.SUCCESS);
    }

    // 채팅 controller 구현 필요

}
