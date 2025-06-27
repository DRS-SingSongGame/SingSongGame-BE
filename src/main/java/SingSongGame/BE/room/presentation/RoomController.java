package SingSongGame.BE.room.application;

import SingSongGame.BE.common.response.ApiResponse;
import SingSongGame.BE.common.response.ApiResponseBody;
import SingSongGame.BE.common.response.ApiResponseGenerator;
import SingSongGame.BE.common.response.MessageCode;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/room")
public class RoomController {

    @Operation(summary = "방 생성")
    @PostMapping
    public ApiResponse<ApiResponseBody.SuccessBody<Void>> createRoom() {
        // 구현 필요
        return ApiResponseGenerator.success(HttpStatus.CREATED, MessageCode.CREATE);
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

    @Operation(summary = "게임 방 목록 조회")
    @GetMapping()
    public ApiResponse<ApiResponseBody.SuccessBody<Void>> getRooms() {
        // 구현 필요
        return ApiResponseGenerator.success(HttpStatus.OK, MessageCode.UPDATE);
    }

    // 채팅 controller 구현 필요

}
