package SingSongGame.BE.quick_match.application.dto.response;

import SingSongGame.BE.quick_match.application.rating.TierChangeResult;

import java.util.List;

public record QuickMatchResultResponse(
        Long roomId,
        List<PlayerResultInfo> players
) {
    public static QuickMatchResultResponse of(Long roomId, List<TierChangeResult> results) {
        List<PlayerResultInfo> infos = results.stream()
                .map(r -> new PlayerResultInfo(
                        r.userId(),
                        r.oldMmr(),
                        r.newMmr(),
                        r.oldTier().getDisplayName(),
                        r.newTier().getDisplayName(),
                        r.getTierStatus()
                ))
                .toList();

        return new QuickMatchResultResponse(roomId, infos);
    }

    public record PlayerResultInfo(
            Long userId,
            int oldMmr,
            int newMmr,
            String oldTier,
            String newTier,
            String tierStatus // "UP", "DOWN", "SAME"
    ) {}
}
