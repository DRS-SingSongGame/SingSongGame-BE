package SingSongGame.BE.ai_game.controller;

import SingSongGame.BE.ai_game.application.AiGameService;
import SingSongGame.BE.ai_game.dto.request.AiAnswerRequest;
import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.common.annotation.LoginUser;
import SingSongGame.BE.common.response.ApiResponse;
import SingSongGame.BE.common.response.ApiResponseBody;
import SingSongGame.BE.common.response.ApiResponseGenerator;
import SingSongGame.BE.common.response.MessageCode;
import SingSongGame.BE.in_game.dto.request.AnswerRequest;
import SingSongGame.BE.in_game.dto.request.GameStartRequest;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.bind.annotation.*;


import java.util.Date;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/ai-game")
@RequiredArgsConstructor
public class AiGameController {

    private final AiGameService aiGameService;
    private final ThreadPoolTaskScheduler taskScheduler;

    @Operation(summary = "AI 게임 시작")
    @PostMapping("/{roomId}/start")
    public ApiResponse<ApiResponseBody.SuccessBody<Void>> startGame(@PathVariable Long roomId, @RequestBody(required = false) GameStartRequest request) {
        log.info("🚀 [게임 시작 요청] roomId: {}", roomId);
        log.info("📝 [요청 데이터] request: {}", request);
        Set<String> keywords = (request != null) ? request.keywords() : Set.of(); // null-safe 처리

        log.info("🏷️  [키워드 처리] keywords: {}", keywords);
        log.info("📊 [키워드 개수] size: {}", keywords.size());
        if (keywords.isEmpty()) {
            log.warn("⚠️  [키워드 없음] 모든 곡에서 랜덤 선택됩니다.");
        } else {
            log.info("✅ [키워드 있음] 다음 키워드로 필터링: {}", String.join(", ", keywords));
        }

        try {
            aiGameService.startGame(roomId, keywords);
            log.info("✅ [게임 시작 완료] roomId: {}", roomId);


            return ApiResponseGenerator.success(HttpStatus.OK, MessageCode.SUCCESS);
        } catch (Exception e) {
            log.error("❌ [게임 시작 실패] roomId: {}, error: {}", roomId, e.getMessage(), e);
            throw e;
        }
    }




    @PostMapping("/{roomId}/tts-finished")
    public ResponseEntity<Void> notifyTtsFinished(@PathVariable Long roomId) {
        System.out.println("✅ 콘솔 로그 - TTS 알림 도착");
        log.info("✅ [notifyTtsFinished] POST 요청 받음 - roomId: {}", roomId);

        taskScheduler.schedule(() -> {
            System.out.println("⏱ 콘솔 로그 - 2초 후 라운드 시작 예정");
            log.info("⏱ [Scheduler] 2초 후 startNextRound 실행!");
            aiGameService.startNextRound(roomId);
        }, new Date(System.currentTimeMillis() + 2000));

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "AI 게임 정답 제출")
    @PostMapping("/{roomId}/answer")
    public ApiResponse<ApiResponseBody.SuccessBody<Void>> submitAnswer(
            @PathVariable Long roomId,
            @RequestBody AiAnswerRequest answerRequest,
            @LoginUser User loginUser) {
        aiGameService.verifyAnswer(loginUser, roomId, answerRequest.getAnswer(), answerRequest.getTimeLeft());
        return ApiResponseGenerator.success(HttpStatus.OK, MessageCode.SUCCESS);
    }

    @Operation(summary = "AI 게임 유저 정보 조회 (구현 예정)")
    @GetMapping("/users")
    public ApiResponse<ApiResponseBody.SuccessBody<Void>> getInGameUsers() {
        // TODO: 구현 필요
        return ApiResponseGenerator.success(HttpStatus.OK, MessageCode.GET);
    }

    @Operation(summary = "AI 게임 정보 조회 (구현 예정)")
    @GetMapping("/info")
    public ApiResponse<ApiResponseBody.SuccessBody<Void>> getInGameInfo() {
        // TODO: 구현 필요
        return ApiResponseGenerator.success(HttpStatus.OK, MessageCode.GET);
    }

    // TODO: WebSocket 채팅 메시지 브로드캐스트용 컨트롤러 or WebSocketMessageMapping 별도 구현
}

