package SingSongGame.BE.quick_match.application.rating;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GlickoPlayer {
    private long userId;
    private double rating;
    private double rd;

    public GlickoPlayer(long userId, double rating, double rd) {
        this.userId = userId;
        this.rating = rating;
        this.rd = rd;
    }
}