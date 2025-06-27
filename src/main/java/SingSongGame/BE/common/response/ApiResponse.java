package SingSongGame.BE.common.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ApiResponse<B> extends ResponseEntity<B> {

    public ApiResponse(final HttpStatus status) {
        super(status);
    }

    public ApiResponse(final B body, final HttpStatus status) {
        super(body, status);
    }
}
