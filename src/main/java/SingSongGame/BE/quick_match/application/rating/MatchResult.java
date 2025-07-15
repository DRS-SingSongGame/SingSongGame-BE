package SingSongGame.BE.quick_match.application.rating;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MatchResult {
    private final long playerId;
    private final double rating;
    private final double rd;
    private final double score;
}

