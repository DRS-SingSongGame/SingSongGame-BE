package SingSongGame.BE.in_game.presentation;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.common.annotation.LoginUser;
import SingSongGame.BE.common.response.ApiResponse;
import SingSongGame.BE.common.response.ApiResponseBody;
import SingSongGame.BE.common.response.ApiResponseGenerator;
import SingSongGame.BE.common.response.MessageCode;
import SingSongGame.BE.in_game.application.InGameService;
import SingSongGame.BE.in_game.dto.request.AnswerRequest;
import SingSongGame.BE.in_game.dto.request.GameStartRequest;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/game-session")
@RequiredArgsConstructor
public class InGameController {

    private final InGameService inGameService;

    @Operation(summary = "게임 시작")
    @PostMapping("/{roomId}/start")
    public ApiResponse<ApiResponseBody.SuccessBody<Void>> startGame(
            @PathVariable("roomId") Long roomId,
            @RequestBody(required = false) GameStartRequest request // <-- 추가
    ) {
        Set<String> keywords = (request != null) ? request.keywords() : Set.of(); // null-safe 처리
        inGameService.startGame(roomId, keywords); // 서비스도 수정 필요
        return ApiResponseGenerator.success(HttpStatus.OK, MessageCode.SUCCESS);
    }

    @Operation(summary = "정답 제출")
    @PostMapping("/{roomId}/answer")
    public ApiResponse<ApiResponseBody.SuccessBody<Void>> submitAnswer(
            @PathVariable("roomId") Long roomId,
            @RequestBody AnswerRequest answerRequest,
            @LoginUser User loginUser) {
        inGameService.verifyAnswer(loginUser, roomId, answerRequest.getAnswer());
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
