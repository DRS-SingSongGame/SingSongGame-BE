package SingSongGame.BE.user.application;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.user.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. id=" + id));
    }

    public User findByName(String username) {
        return userRepository.findByName(username)
                .orElse(null);
    }

    @Transactional
    public User updateName(Long userId, String username) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다"));

        user.updateUserName(username);
        return user; // save 생략 가능 (영속성 컨텍스트 내부에서 자동 반영)
    }

    public Boolean isAvailableName (String username) {
        return userRepository.findByName(username).isEmpty();
    }
}
