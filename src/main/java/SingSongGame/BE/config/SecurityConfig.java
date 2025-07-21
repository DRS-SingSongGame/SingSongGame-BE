package SingSongGame.BE.config;

import SingSongGame.BE.auth.filter.JwtAuthenticationFilter;
import SingSongGame.BE.auth.handler.CustomOAuth2UserService;
import SingSongGame.BE.auth.handler.OAuth2LoginSuccessHandler;
import SingSongGame.BE.common.util.JwtProvider;
import SingSongGame.BE.user.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/health").permitAll()  // 헬스체크 허용
                        .requestMatchers("/api/auth/nickname").authenticated()
                        .requestMatchers("/api/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                                     .userInfoEndpoint(userInfo ->userInfo
                                             .userService(new CustomOAuth2UserService())
                                     )
                                     .successHandler(oAuth2LoginSuccessHandler)
//                        .defaultSuccessUrl("/auth/success", true)
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider, userRepository),
                                 UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}