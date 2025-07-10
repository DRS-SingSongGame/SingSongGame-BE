package SingSongGame.BE.in_game.dto.request;

import java.util.Set;

public record GameStartRequest(
        Set<String> keywords
) {
}
