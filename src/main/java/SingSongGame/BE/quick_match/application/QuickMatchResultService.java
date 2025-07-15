package SingSongGame.BE.quick_match.application;

import SingSongGame.BE.quick_match.application.rating.*;
import SingSongGame.BE.quick_match.persistence.QuickMatchRoom;
import SingSongGame.BE.quick_match.persistence.QuickMatchRoomPlayer;
import SingSongGame.BE.room.persistence.RoomPlayer;
import SingSongGame.BE.user.persistence.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


@Service
@Transactional
@RequiredArgsConstructor
public class QuickMatchResultService {

    private final UserRepository userRepository;
    private final GlickoRatingService glickoRatingService;
    private static final Logger log = LoggerFactory.getLogger(QuickMatchResultService.class);

    public List<TierChangeResult> processResult(List<RoomPlayer> roomPlayers) {
        List<TierChangeResult> resultList = new ArrayList<>();
        // 1. GlickoPlayer 변환
        List<GlickoPlayer> glickoPlayers = roomPlayers.stream()
                .map(p -> new GlickoPlayer(
                        p.getUser().getId(),
                        p.getUser().getQuickMatchMmr(),
                        350.0
                )).toList();

        List<RoomPlayer> sorted = new ArrayList<>(roomPlayers);
        sorted.sort(Comparator.comparingInt(RoomPlayer::getScore).reversed());

        double[] glickoScores = {1.0, 0.8, 0.6, 0.4, 0.2, 0.0};
        Map<Long, Double> glickoScoreMap = new HashMap<>();
        for (int i = 0; i < sorted.size(); i++) {
            glickoScoreMap.put(sorted.get(i).getUser().getId(), glickoScores[i]);
        }

        // 2. MatchResult 생성
        List<MatchResult> matchResults = roomPlayers.stream()
                .map(p -> new MatchResult(
                        p.getUser().getId(),
                        p.getUser().getQuickMatchMmr(),
                        350.0,
                        glickoScoreMap.getOrDefault(p.getUser().getId(), 0.0)
                ))
                .toList();

        // 3. Glicko 계산
        for (GlickoPlayer player : glickoPlayers) {
            List<MatchResult> opponents = matchResults.stream()
                    .filter(r -> r.getPlayerId() != player.getUserId())
                    .toList();

            log.info("🎯 [Glicko 계산 시작] playerId={}, baseRating={}", player.getUserId(), player.getRating());

            for (MatchResult r : opponents) {
                log.info("🆚 상대 정보: id={}, score={}, rating={}", r.getPlayerId(), r.getScore(), r.getRating());
            }

            glickoRatingService.updatePlayer(player, opponents);
        }

        // 4. User MMR 저장
        for (GlickoPlayer updated : glickoPlayers) {
            userRepository.findById(updated.getUserId()).ifPresent(user -> {
                int oldMmr = user.getQuickMatchMmr();
                Tier oldTier = Tier.fromMmr(oldMmr);
                int newMmr = (int) Math.round(updated.getRating());
                Tier newTier = Tier.fromMmr(newMmr);

                user.updateQuickMatchMmr(newMmr);
                userRepository.save(user);

                log.info("✅ MMR 및 티어 업데이트: {} → {}, {} → {}", oldMmr, newMmr, oldTier.getDisplayName(), newTier.getDisplayName());

                resultList.add(new TierChangeResult(
                        user.getId(), oldMmr, newMmr, oldTier, newTier
                ));
            });
        }
        return resultList;
    }

    public List<TierChangeResult> processQuickMatchResult(List<QuickMatchRoomPlayer> quickPlayers) {
        List<GlickoPlayer> glickoPlayers = quickPlayers.stream()
                .map(p -> new GlickoPlayer(
                        p.getUser().getId(),
                        p.getUser().getQuickMatchMmr(),
                        350.0
                ))
                .toList();

        List<QuickMatchRoomPlayer> sorted = new ArrayList<>(quickPlayers);
        sorted.sort(Comparator.comparingInt(QuickMatchRoomPlayer::getScore).reversed());

        double[] glickoScores = {1.0, 0.8, 0.6, 0.4, 0.2, 0.0};
        Map<Long, Double> glickoScoreMap = new HashMap<>();
        for (int i = 0; i < sorted.size(); i++) {
            glickoScoreMap.put(sorted.get(i).getUser().getId(), glickoScores[i]);
        }

        List<MatchResult> matchResults = quickPlayers.stream()
                .map(p -> new MatchResult(
                        p.getUser().getId(),
                        p.getUser().getQuickMatchMmr(),
                        350.0,
                        glickoScoreMap.getOrDefault(p.getUser().getId(), 0.0)
                ))
                .toList();

        for (GlickoPlayer player : glickoPlayers) {
            List<MatchResult> opponents = matchResults.stream()
                    .filter(r -> r.getPlayerId() != player.getUserId())
                    .toList();
            glickoRatingService.updatePlayer(player, opponents);
        }

        List<TierChangeResult> resultList = new ArrayList<>();
        for (GlickoPlayer updated : glickoPlayers) {
            userRepository.findById(updated.getUserId()).ifPresent(user -> {
                int oldMmr = user.getQuickMatchMmr();
                Tier oldTier = Tier.fromMmr(oldMmr);
                int newMmr = (int) Math.round(updated.getRating());
                Tier newTier = Tier.fromMmr(newMmr);

                user.updateQuickMatchMmr(newMmr);
                userRepository.save(user);

                resultList.add(new TierChangeResult(
                        user.getId(), oldMmr, newMmr, oldTier, newTier
                ));
            });
        }

        return resultList;
    }

}
