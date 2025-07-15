package SingSongGame.BE.quick_match.presentation;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.common.response.ApiResponseGenerator;
import SingSongGame.BE.common.response.MessageCode;
import SingSongGame.BE.quick_match.application.QuickLogicService;
import SingSongGame.BE.quick_match.application.QuickMatchQueueService;
import SingSongGame.BE.quick_match.application.QuickMatchService;
import SingSongGame.BE.quick_match.application.dto.request.QuickMatchRequest;
import SingSongGame.BE.quick_match.application.dto.response.QuickMatchResultResponse;
import SingSongGame.BE.quick_match.application.rating.TierChangeResult;
import SingSongGame.BE.quick_match.persistence.QuickMatchRoom;
import SingSongGame.BE.user.application.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("quick-match")
public class QuickMatchController {

    private final QuickMatchQueueService quickMatchQueueService;
    private final UserService userService;
    private final QuickLogicService quickLogicService;
    private final QuickMatchService quickMatchService;
    private static final Logger log = LoggerFactory.getLogger(QuickMatchController.class);

    @PostMapping("/enter")
    public ResponseEntity<?> enterQuickMatch(@RequestParam Long userId) {
        User user = userService.findById(userId);
        quickMatchQueueService.addToQueue(user);
        quickLogicService.tryMatch(user);
        return ApiResponseGenerator.success(HttpStatus.OK, MessageCode.SUCCESS);

    }

    @PostMapping("/end")
    public ResponseEntity<?> endQuickMatch(@RequestParam String roomCode) {
        log.info("📥 [POST] /api/quick-match/end 호출됨 - roomCode={}", roomCode);

        QuickMatchRoom room = quickMatchService.findByRoomCode(roomCode);
        log.info("✅ QuickMatchRoom 조회 완료 - roomId={}, roomStarted={}", room.getRoom().getId(), room.isGameStarted());

        List<TierChangeResult> resultList = quickMatchService.endGame(room);
        log.info("📤 게임 종료 및 MMR 계산 완료 - 변경된 유저 수={}", resultList.size());

        QuickMatchResultResponse response = QuickMatchResultResponse.of(
                room.getRoom().getId(),
                resultList
        );

        return ApiResponseGenerator.success(response, HttpStatus.OK, MessageCode.SUCCESS);
    }
}
