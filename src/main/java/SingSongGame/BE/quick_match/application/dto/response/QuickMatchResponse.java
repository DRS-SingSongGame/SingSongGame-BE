package SingSongGame.BE.quick_match.application.dto.response;

import SingSongGame.BE.quick_match.persistence.QuickMatchRoom;
import SingSongGame.BE.quick_match.persistence.QuickMatchRoomPlayer;

import java.util.List;
import java.util.stream.Collectors;

public record QuickMatchResponse (
        Long roomId,
        String roomCode,
        int averageMmr,
        int roundCount,
        String mode,
        String roomType,
        List<QuickMatchPlayerInfo> players
) {
    public static QuickMatchResponse from(QuickMatchRoom room) {
        List<QuickMatchPlayerInfo> playerInfos = room.getPlayers().stream()
                .map(p -> new QuickMatchPlayerInfo(
                        p.getUser().getId(),
                        p.getUser().getName(),
                        p.getUser().getImageUrl(),
                        p.getMmrAtMatchTime(),
                        p.getScore()
                ))
                .collect(Collectors.toList());

        return new QuickMatchResponse(
                room.getRoom().getId(),
                room.getRoomCode(),
                room.getAverageMmr(),
                room.getRoundCount(),
                room.getMode(),
                room.getRoom().getRoomType().name(),
                playerInfos
        );
    }

    public record QuickMatchPlayerInfo (
            Long userId,
            String nickname,
            String profileImage,
            int mmrAtMatchTime,
            int score
    ) {}
}
