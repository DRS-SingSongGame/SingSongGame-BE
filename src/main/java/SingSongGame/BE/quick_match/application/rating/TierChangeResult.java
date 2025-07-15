package SingSongGame.BE.quick_match.application.rating;

public record TierChangeResult(
        Long userId,
        int oldMmr,
        int newMmr,
        Tier oldTier,
        Tier newTier
) {
    public String getTierStatus() {
        if (oldTier == newTier) return "SAME";
        return newTier.ordinal() > oldTier.ordinal() ? "UP" : "DOWN";
    }
}
