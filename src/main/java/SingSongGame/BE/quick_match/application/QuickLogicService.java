package SingSongGame.BE.quick_match.application;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.user.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class QuickLogicService {

    private final QuickMatchQueueService quickMatchQueueService;
    private final QuickMatchService QuickMatchService;
    private final UserRepository userRepository;

    public void tryMatch(User user) {
        int baseMmr = user.getQuickMatchMmr();
        int expand = 0;

        while (expand <= 3000) {
            int min = baseMmr - 50 -expand;
            int max = baseMmr + 50 +expand;

            Set<Object> candidates = quickMatchQueueService.getCandidatesInRange(min, max);
            if(candidates.size() >= 2){
                List<User> matchedUsers = selectTop6(candidates);
                QuickMatchService.startQuickMatchGame(matchedUsers);
                matchedUsers.forEach(u -> quickMatchQueueService.removeFromQueue(u.getId()));
                return;
            }
            expand += 50;
        }
    }

    private List<User> selectTop6(Set<Object> candidates) {
        List<Long> ids = candidates.stream()
                .map(obj -> {
                    if (obj instanceof Integer) {
                        return ((Integer) obj).longValue();
                    } else if (obj instanceof Long) {
                        return (Long) obj;
                    } else if (obj instanceof String) {
                        return Long.parseLong((String) obj);
                    } else {
                        throw new IllegalArgumentException("Unexpected ID type: " + obj.getClass());
                    }
                })
                .limit(6)
                .toList();

        return userRepository.findAllById(ids);
    }
}
