package SingSongGame.BE.online.presentation;

import SingSongGame.BE.online.application.OnlineUserService;
import SingSongGame.BE.online.application.dto.response.OnlineUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

    @RestController
    @RequestMapping("/online-users")
    @RequiredArgsConstructor
    public class OnlineUserController {

        private final OnlineUserService onlineUserService;

        @GetMapping
        public ResponseEntity<List<OnlineUserResponse>> getOnlineUsers() {
            return ResponseEntity.ok(onlineUserService.getAllOnlineUsers());
        }
    }
