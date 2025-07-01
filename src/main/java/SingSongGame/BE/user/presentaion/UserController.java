package SingSongGame.BE.user.presentaion;
import SingSongGame.BE.common.response.MessageCode;
import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.common.annotation.LoginUser;
import SingSongGame.BE.common.response.ApiResponse;
import SingSongGame.BE.common.response.ApiResponseBody;
import SingSongGame.BE.common.response.ApiResponseGenerator;
import SingSongGame.BE.user.application.UserService;
import SingSongGame.BE.user.application.dto.response.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<ApiResponseBody.SuccessBody<UserResponse>> getMyInfo(@LoginUser User user) {
        UserResponse response = new UserResponse(user);
        return ApiResponseGenerator.success(response, HttpStatus.OK, MessageCode.SUCCESS);
    }

    @GetMapping("/check-name")
    public ApiResponse<ApiResponseBody.SuccessBody<Boolean>> checkName(@RequestParam("name") String name) {
        boolean isAvailable = userService.isAvailableName(name);
        return ApiResponseGenerator.success(isAvailable, HttpStatus.OK, MessageCode.SUCCESS);
    }

}
