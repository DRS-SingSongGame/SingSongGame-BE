package SingSongGame.BE.user.presentaion;
import SingSongGame.BE.common.response.MessageCode;
import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.common.annotation.LoginUser;
import SingSongGame.BE.common.response.ApiResponse;
import SingSongGame.BE.common.response.ApiResponseBody;
import SingSongGame.BE.common.response.ApiResponseGenerator;
import SingSongGame.BE.user.application.dto.response.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/me")
    public ApiResponse<ApiResponseBody.SuccessBody<UserResponse>> getMyInfo(@LoginUser User user) {
        UserResponse response = new UserResponse(user);
        return ApiResponseGenerator.success(response, HttpStatus.OK, MessageCode.SUCCESS);
    }


}
