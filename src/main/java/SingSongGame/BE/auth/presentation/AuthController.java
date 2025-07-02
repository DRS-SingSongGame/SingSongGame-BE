package SingSongGame.BE.auth.presentation;

import SingSongGame.BE.auth.application.AuthService;
import SingSongGame.BE.auth.application.dto.response.TokenResponse;
import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.common.annotation.LoginUser;
import SingSongGame.BE.common.response.ApiResponse;
import SingSongGame.BE.common.response.ApiResponseBody;
import SingSongGame.BE.common.response.ApiResponseBody.SuccessBody;
import SingSongGame.BE.common.response.ApiResponseGenerator;
import SingSongGame.BE.common.response.MessageCode;
import SingSongGame.BE.common.util.CookieUtil;

import SingSongGame.BE.user.application.UserService;
import SingSongGame.BE.user.application.dto.request.NameRequest;
import SingSongGame.BE.user.application.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    private final UserService userService;

    private final Environment environment;

    public AuthController(AuthService authService, UserService userService, Environment environment) {
        this.authService = authService;
        this.userService = userService;
        this.environment = environment;
    }

//    @Operation(
//            summary = "로그인을 한다.",
//            description = "카카오 Oauth 2.0을 통해 로그인을 진행한다. " +
//                    "첫 유저일 경우, 카카오 로그인 및 회원가입을 동시에 진행 후 닉네임 설정 화면으로 리다이렉트, " +
//                    "기존 유저일 경우, 바로 게임 대기실로 리다이렉트"
//    )
//    @PostMapping("/login")
//    public ApiResponse<SuccessBody<Void>> login() {
//        return ApiResponseGenerator.success(HttpStatus.CREATED, MessageCode.LOGIN);
//    }

    @Operation(
            summary = "로그아웃한다.",
            description = "쿠키에 담긴 리프레시 토큰을 이용하여 로그아웃한다.")
    @PostMapping("/logout")
    public ApiResponse<SuccessBody<Void>> logout(HttpServletResponse response) {
        boolean isSecure = !Arrays.asList(environment.getActiveProfiles()).contains("dev");

        CookieUtil.deleteSameSiteCookie(response, "access_token", isSecure);
        CookieUtil.deleteSameSiteCookie(response, "refresh_token", isSecure);

        return ApiResponseGenerator.success(HttpStatus.OK, MessageCode.LOGOUT);
    }

    @Operation(
            summary = "토큰을 재발급한다.",
            description = "쿠키에 담긴 사용자 토큰을 이용하여 리프레시 토큰을 반환한다.")
    @PostMapping("/reissue")
    public ApiResponse<SuccessBody<Void>> reissue(@CookieValue("refresh_token") String refreshToken) {
        TokenResponse token = authService.reissue(refreshToken);
        return ApiResponseGenerator.success(HttpStatus.OK, MessageCode.REISSUE);
    }

    @GetMapping("/success")
    public void redirect(HttpServletResponse response) throws IOException {
        response.sendRedirect("http://localhost:3000/lobby"); // or nickname
    }

    @PutMapping("/nickname")
    public ApiResponse<ApiResponseBody.SuccessBody<Void>> setNickname(
            @RequestBody NameRequest request,
            @LoginUser User user) {

        userService.updateName(user.getId(), request.getName());
        return ApiResponseGenerator.success(HttpStatus.OK,  MessageCode.UPDATE);
    }

    @GetMapping("/nickname")
    public ResponseEntity<Void> nicknamePing() {
        return ResponseEntity.ok().build();
    }

}
