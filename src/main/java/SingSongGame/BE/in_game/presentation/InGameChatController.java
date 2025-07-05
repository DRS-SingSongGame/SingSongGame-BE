package SingSongGame.BE.in_game.presentation;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.common.annotation.LoginUser;
import SingSongGame.BE.in_game.application.InGameChatService;
import SingSongGame.BE.chat.dto.RoomChatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class InGameChatController {

    private final InGameChatService inGameChatService;

    @MessageMapping("/in-game/{roomId}/chat")
    public void sendInGameChatMessage(@LoginUser User user, @DestinationVariable("roomId") Long roomId, RoomChatRequest request) {
        inGameChatService.verifyAnswer(user, roomId, request.getMessage());
    }
}
