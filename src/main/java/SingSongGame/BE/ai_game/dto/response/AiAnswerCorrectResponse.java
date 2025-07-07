package SingSongGame.BE.ai_game.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiAnswerCorrectResponse {
    private String winnerNickname;
    private String correctAnswer;
}
