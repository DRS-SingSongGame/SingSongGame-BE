package SingSongGame.BE.in_game.dto.response;

import java.util.List;

public record GameEndResponse(
        List<FinalResult> finalResults
) {
}
