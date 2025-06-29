package SingSongGame.BE.common.response;

import org.springframework.http.HttpStatus;

public class ApiResponseGenerator {

    public static ApiResponse<ApiResponseBody.SuccessBody<Void>> success(
            final HttpStatus status, MessageCode code) {
        return new ApiResponse<>(
                new ApiResponseBody.SuccessBody<>(null, code.getCode(), code.getMessage()), status);
    }

    public static <D> ApiResponse<ApiResponseBody.SuccessBody<D>> success(
            final D data, final HttpStatus status, MessageCode code) {
        return new ApiResponse<>(
                new ApiResponseBody.SuccessBody<>(data, code.getCode(), code.getMessage()), status);
    }

    public static ApiResponse<ApiResponseBody.FailureBody> fail(
            final String message, final String code, final HttpStatus status) {
        return new ApiResponse<>(
                new ApiResponseBody.FailureBody(String.valueOf(status.value()), code, message), status);
    }
}
