package SingSongGame.BE.quick_match.presentation;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.common.response.ApiResponseGenerator;
import SingSongGame.BE.common.response.MessageCode;
import SingSongGame.BE.in_game.application.InGameService;
import SingSongGame.BE.quick_match.application.QuickLogicService;
import SingSongGame.BE.quick_match.application.QuickMatchQueueService;
import SingSongGame.BE.quick_match.application.QuickMatchService;
import SingSongGame.BE.quick_match.application.dto.request.QuickMatchRequest;
import SingSongGame.BE.quick_match.application.dto.response.QuickMatchResultResponse;
import SingSongGame.BE.quick_match.application.rating.TierChangeResult;
import SingSongGame.BE.quick_match.cache.QuickMatchResultCache;
import SingSongGame.BE.quick_match.persistence.QuickMatchRepository;
import SingSongGame.BE.quick_match.persistence.QuickMatchRoom;
import SingSongGame.BE.user.application.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("quick-match")
public class QuickMatchController {

    private final QuickMatchQueueService quickMatchQueueService;
    private final UserService userService;
    private final QuickLogicService quickLogicService;
    private final QuickMatchService quickMatchService;
    private static final Logger log = LoggerFactory.getLogger(QuickMatchController.class);
    private final QuickMatchRepository quickMatchRoomRepository;
    private final InGameService inGameService;
    private final QuickMatchResultCache quickMatchResultCache;

    @PostMapping("/enter")
    public ResponseEntity<?> enterQuickMatch(@RequestParam Long userId) {
        User user = userService.findById(userId);
        quickMatchQueueService.addToQueue(user);
        quickLogicService.tryMatch(user);
        return ApiResponseGenerator.success(HttpStatus.OK, MessageCode.SUCCESS);

    }

    @GetMapping("/result")
    public ResponseEntity<?> getResult(@RequestParam String roomCode) {
        log.info("📥 [GET] /result 요청됨 - roomCode={}", roomCode);
        QuickMatchRoom room = quickMatchRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("해당 룸을 찾을 수 없습니다."));

        List<TierChangeResult> result = quickMatchResultCache.get(roomCode);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("아직 MMR 결과가 준비되지 않았습니다.");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("roomId", room.getId());
        response.put("players", result);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/end")
    public ResponseEntity<?> endQuickMatch(@RequestParam String roomCode) {
        log.info("📥 [POST] /api/quick-match/end 호출됨 - roomCode={}", roomCode);

        QuickMatchRoom room = quickMatchService.findByRoomCode(roomCode);
        log.info("✅ QuickMatchRoom 조회 완료 - roomId={}, roomStarted={}", room.getRoom().getId(), room.isGameStarted());

        return ApiResponseGenerator.success(null, HttpStatus.OK, MessageCode.SUCCESS);
    }

    @PostMapping("/leave")
    public ResponseEntity<Void> leaveQueue(@RequestBody QuickMatchRequest quickMatchRequest) {
        quickMatchQueueService.removeFromQueue(quickMatchRequest.userId());
        return ResponseEntity.ok().build();
    }
}
