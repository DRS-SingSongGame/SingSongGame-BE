package SingSongGame.BE.room_keyword;

import SingSongGame.BE.room_keyword.dto.KeywordConfirmRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class KeywordController {

    private final KeywordService keywordService;
    private final SimpMessagingTemplate messagingTemplate;

    // 방장이 키워드 확정 → 저장 + 참여자에게 전달
    @MessageMapping("/keyword/confirm")
    public void confirmKeywords(KeywordConfirmRequest request) {
        keywordService.saveKeywords(request.getRoomId(), request.getKeywords());

        // 브로드캐스트 → 모든 참여자에게 키워드 전송
        messagingTemplate.convertAndSend(
                "/topic/room/" + request.getRoomId() + "/keywords",
                request.getKeywords()
        );
    }
}
