package SingSongGame.BE.quick_match.application.rating;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GlickoRatingService {

    private static final double INITIAL_RATING = 1500.0;
    private static final double INITIAL_RD = 350.0;
    private static final double Q = Math.log(10) / 400;

    public void updatePlayer(GlickoPlayer player, List<MatchResult> matches) {
        if(matches.isEmpty()) return;

        double d2InvSum = 0.0;
        double deltaSum = 0.0;

        for (MatchResult match : matches) {
            double g = g(match.getRd());
            double E = E(player.getRating(), match.getRating(), match.getRd());

            d2InvSum += g * g * E * (1 - E);
            deltaSum += g * (match.getScore() - E);
        }

        double d2 = 1.0 / (Q * Q * d2InvSum);
        double newRating = player.getRating() + Q / ((1.0 / (player.getRd() * player.getRd())) + (1.0 / d2)) * deltaSum;
        double newRD = Math.sqrt(1.0 / ((1.0 / (player.getRd() * player.getRd())) + (1.0 / d2)));

        player.setRating(newRating);
        player.setRd(newRD);
    }

    private double g(double rd){
        return 1.0 / Math.sqrt(1.0 + (3.0 * Q * Q * rd * rd) / (Math.PI * Math.PI));
    }

    private double E(double r1, double r2, double rd2) {
        return 1.0 / (1.0 + Math.pow(10.0, -g(rd2) * (r1 - r2) / 400.0));
    }

    public GlickoPlayer createInitialPlayer(long userId) {
        return new GlickoPlayer(userId, INITIAL_RATING, INITIAL_RD);
    }
}
