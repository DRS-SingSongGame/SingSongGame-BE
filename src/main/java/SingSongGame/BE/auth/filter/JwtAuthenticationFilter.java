package SingSongGame.BE.auth.filter;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.common.util.JwtProvider;
import SingSongGame.BE.user.persistence.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        String token = jwtProvider.extractTokenFromCookie(request);
        System.out.println("🔥 요청 URI: " + request.getRequestURI());
        System.out.println("🔥 access_token 쿠키: " + token);

        if (token != null && jwtProvider.validateToken(token)) {
            Long userId = jwtProvider.getUserIdFromToken(token);
            User user = userRepository.findById(userId).orElse(null);
            System.out.println("✅ userId: " + userId);

            if (user != null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("✅ 인증 객체 설정 완료: " + user.getEmail());
            }
            System.out.println("[DEBUG] 토큰: " + token);
            System.out.println("[DEBUG] 유저 ID: " + userId);
            System.out.println("[DEBUG] 인증 성공 여부: " + (user != null));
        }

        filterChain.doFilter(request, response);
    }
}
