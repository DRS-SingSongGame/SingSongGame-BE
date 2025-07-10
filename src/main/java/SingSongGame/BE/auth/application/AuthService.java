package SingSongGame.BE.auth.application;

import SingSongGame.BE.auth.application.dto.response.LoginResponse;
import SingSongGame.BE.auth.application.dto.response.TokenResponse;
import SingSongGame.BE.auth.persistence.AuthRepository;
import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.common.util.CookieUtil;
import SingSongGame.BE.common.util.JwtProvider;
import SingSongGame.BE.online.application.OnlineUserService;
import SingSongGame.BE.online.persistence.OnlineLocation;
import SingSongGame.BE.online.persistence.OnlineUser;
import SingSongGame.BE.online.persistence.OnlineUserRepository;
import SingSongGame.BE.user.persistence.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRepository authRepository;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final Environment env;

    private static final String DEFAULT_IMAGE_URL = "";

    public LoginResponse handleOAuthLogin(OAuth2User oAuth2User, HttpServletResponse response) {

        Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
        String email = (String) kakaoAccount.get("email");

        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        String imageUrl = (profile != null) ? (String) profile.get("profile_image_url") : DEFAULT_IMAGE_URL;

        User user = authRepository.findByEmail(email)
                .orElseGet(() -> registerUser(email, imageUrl));

        String accessToken = jwtProvider.createAccessToken(user);
        String refreshToken = jwtProvider.createRefreshToken(user);

        boolean isSecure = !Arrays.asList(env.getActiveProfiles()).contains("dev");

        CookieUtil.addSameSiteCookie(response, "access_token", accessToken, isSecure);
        CookieUtil.addSameSiteCookie(response, "refresh_token", refreshToken, isSecure);

        return new LoginResponse(
                accessToken,
                refreshToken,
                user.isFirstLogin(),
                user.getId(),
                user.getEmail(),
                user.getName()
        );
    }

    public TokenResponse reissue(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token");
        }

        Claims claims = jwtProvider.parseClaims(refreshToken);
        String email = claims.getSubject();

        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String newAccessToken = jwtProvider.createAccessToken(user);

        return new TokenResponse(newAccessToken, user.getId());
    }

    private User registerUser(String email, String imageUrl) {
        User user = User.builder()
                .email(email)
                .imageUrl(imageUrl)
                .name(null)
                .isFirstLogin(true)
                .build();

        return userRepository.save(user);
    }
}
