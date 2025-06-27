package SingSongGame.BE.in_game.presentation;

import SingSongGame.BE.common.response.ApiResponse;
import SingSongGame.BE.common.response.ApiResponseBody;
import SingSongGame.BE.common.response.ApiResponseGenerator;
import SingSongGame.BE.common.response.MessageCode;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/in-game")
public class InGameController {

    @Operation(summary = "인 게임 유저 정보 조회")
    @GetMapping("/users")
    public ApiResponse<ApiResponseBody.SuccessBody<Void>> getInGameUsers() {
        // 구현 필요
        return ApiResponseGenerator.success(HttpStatus.OK, MessageCode.GET);
    }

    @Operation(summary = "인 게임 정보 조회")
    @GetMapping("/info")
    public ApiResponse<ApiResponseBody.SuccessBody<Void>> getInGameInfo() {
        // 구현 필요
        return ApiResponseGenerator.success(HttpStatus.OK, MessageCode.GET);
    }

    // 채팅 기능 Controller 구현 필요.
}
