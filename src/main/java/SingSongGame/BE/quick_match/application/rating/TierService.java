package SingSongGame.BE.quick_match.application.rating;

import org.springframework.stereotype.Service;

@Service
public class TierService {

    public Tier getTierByMmr(int mmr) {
        return Tier.fromMmr(mmr);
    }

    public String getTierLabel(Tier tier) {
        return switch (tier) {
            case SAENAEGI -> "새내기";
            case HUNRYUNSAENG -> "훈련생";
            case MOHEMGA -> "모험가";
            case DOJEONJA -> "도전자";
            case ACE -> "에이스";
            case LEGEND -> "전설";
        };
    }
}