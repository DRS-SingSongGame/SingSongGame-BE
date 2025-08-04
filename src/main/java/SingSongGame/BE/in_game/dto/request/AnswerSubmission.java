package SingSongGame.BE.in_game.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerSubmission implements Serializable {
    private String requestId;
    private Long userId;
    private String userName;
    private Long roomId;
    private String answer;
    private LocalDateTime answerTime;
}