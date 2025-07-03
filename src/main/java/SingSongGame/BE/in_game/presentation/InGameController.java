package SingSongGame.BE.in_game.presentation;

import SingSongGame.BE.common.response.ApiResponse;
import SingSongGame.BE.common.response.ApiResponseBody;
import SingSongGame.BE.common.response.ApiResponseGenerator;
import SingSongGame.BE.common.response.MessageCode;
import SingSongGame.BE.in_game.application.InGameService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/in-game")
@RequiredArgsConstructor
public class InGameController {

    private final InGameService inGameService;

    @Operation(summary = "게임 시작")
    @PostMapping("/{roomId}/start")
    public ApiResponse<ApiResponseBody.SuccessBody<Void>> startGame(@PathVariable Long roomId) {
        inGameService.startGame(roomId);
        return ApiResponseGenerator.success(HttpStatus.OK, MessageCode.SUCCESS);
    }

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
