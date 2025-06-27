package SingSongGame.BE.auth.presentation;

import SingSongGame.BE.common.response.ApiResponse;
import SingSongGame.BE.common.response.ApiResponseBody.SuccessBody;
import SingSongGame.BE.common.response.ApiResponseGenerator;
import SingSongGame.BE.common.response.MessageCode;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Operation(
            summary = "로그인을 한다.",
            description = "카카오 Oauth 2.0을 통해 로그인을 진행한다. " +
                    "첫 유저일 경우, 카카오 로그인 및 회원가입을 동시에 진행 후 닉네임 설정 화면으로 리다이렉트, " +
                    "기존 유저일 경우, 바로 게임 대기실로 리다이렉트"
    )
    @PostMapping("/login")
    public ApiResponse<SuccessBody<Void>> login() {
        // 구현 필요
        return ApiResponseGenerator.success(HttpStatus.CREATED, MessageCode.LOGIN);
    }

    @Operation(
            summary = "로그아웃한다.",
            description = "쿠키에 담긴 리프레시 토큰을 이용하여 로그아웃한다.")
    @PostMapping("/logout")
    public ApiResponse<SuccessBody<Void>> logout() {
        // 구현 필요
        return ApiResponseGenerator.success(HttpStatus.OK, MessageCode.LOGOUT);
    }

    @Operation(
            summary = "토큰을 재발급한다.",
            description = "쿠키에 담긴 사용자 토큰을 이용하여 리프레시 토큰을 반환한다.")
    @PostMapping("/reissue")
    public ApiResponse<SuccessBody<Void>> reissue() {
        // 구현 필요
        return ApiResponseGenerator.success(HttpStatus.OK, MessageCode.REISSUE);
    }



}
