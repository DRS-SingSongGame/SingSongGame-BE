package SingSongGame.BE.auth.handler;

import SingSongGame.BE.auth.application.AuthService;
import SingSongGame.BE.auth.application.dto.response.LoginResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;

    @Value("${FRONTEND_REDIRECT_URL_LOCAL}")
    private String localRedirectBase;

    @Value("${FRONTEND_REDIRECT_URL_PROD}")
    private String prodRedirectBase;

    private final Environment environment;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        LoginResponse loginResponse = authService.handleOAuthLogin(oAuth2User, response);

        boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        String baseUrl = isProd ? prodRedirectBase : localRedirectBase;

        String redirectUri = loginResponse.isFirstLogin()
                ? baseUrl + "/nickname"
                : baseUrl + "/lobby";

        System.out.println("로그인 완료 - isFirstLogin: " + loginResponse.isFirstLogin());
        System.out.println("리다이렉트 URI: " + redirectUri);

        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }
}
