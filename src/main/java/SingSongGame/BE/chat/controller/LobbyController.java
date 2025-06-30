package SingSongGame.BE.chat.controller;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.chat.service.LobbyChatService;
import SingSongGame.BE.common.annotation.LoginUser;
import SingSongGame.BE.common.response.ApiResponse;
import SingSongGame.BE.common.response.ApiResponseBody;
import SingSongGame.BE.common.response.ApiResponseGenerator;
import SingSongGame.BE.common.response.MessageCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/lobby")
@RequiredArgsConstructor
public class LobbyController {

    private final LobbyChatService lobbyChatService;

    @PostMapping("/enter")
    public ApiResponse<ApiResponseBody.SuccessBody<Void>> enterLobby() {
        log.info("로비 입장 요청");
        // 임시로 더미 사용자 생성
        User dummyUser = User.builder()
                .id(1L)
                .name("테스트_사용자_" + System.currentTimeMillis() % 1000)
                .build();
        lobbyChatService.sendUserEnterLobby(dummyUser);
        return ApiResponseGenerator.success(HttpStatus.OK, MessageCode.ENTER_LOBBY);
    }

    @PostMapping("/leave")
    public ApiResponse<ApiResponseBody.SuccessBody<Void>> leaveLobby() {
        log.info("로비 퇴장 요청");
        // 임시로 더미 사용자 생성
        User dummyUser = User.builder()
                .id(1L)
                .name("테스트_사용자_" + System.currentTimeMillis() % 1000)
                .build();
        lobbyChatService.sendUserLeaveLobby(dummyUser);
        return ApiResponseGenerator.success(HttpStatus.OK, MessageCode.EXIT_LOBBY);
    }
} 