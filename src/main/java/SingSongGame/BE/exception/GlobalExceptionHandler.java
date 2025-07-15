package SingSongGame.BE.exception;

import SingSongGame.BE.common.response.ApiResponse;
import SingSongGame.BE.common.response.ApiResponseBody;
import SingSongGame.BE.common.response.ApiResponseGenerator;
import SingSongGame.BE.common.response.MessageCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomBadRequestException.class)
    public ResponseEntity<ApiResponse<ApiResponseBody.FailureBody>> handleBadRequest(CustomBadRequestException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseGenerator.fail(
                        MessageCode.FAIL.getMessage(),
                        MessageCode.FAIL.getCode(),
                        HttpStatus.BAD_REQUEST
                ));
    }

    // 혹은 범용 처리
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<ApiResponseBody.FailureBody>> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ApiResponseGenerator.fail(
                        ex.getReason(),                    // ✅ 동적 메시지 사용
                        MessageCode.FAIL.getCode(),
                        (HttpStatus) ex.getStatusCode()
                ));
    }
}
