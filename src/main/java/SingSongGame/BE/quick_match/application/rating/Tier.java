package SingSongGame.BE.quick_match.application.rating;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Tier {
    SAENAEGI("새내기", 800, 949),
    HUNRYUNSAENG("훈련생", 950, 1099),
    MOHEMGA("모험가", 1100, 1249),
    DOJEONJA("도전자", 1250, 1399),
    ACE("에이스", 1400, 1549),
    LEGEND("전설", 1550, Integer.MAX_VALUE);

    private final String displayName;
    private final int minMmr;
    private final int maxMmr;

    Tier(String displayName, int minMmr, int maxMmr) {
        this.displayName = displayName;
        this.minMmr = minMmr;
        this.maxMmr = maxMmr;
    }

    public static Tier fromMmr(int mmr) {
        return Arrays.stream(values())
                .filter(tier -> mmr >= tier.minMmr && mmr <= tier.maxMmr)
                .findFirst()
                .orElse(SAENAEGI);
    }
}
