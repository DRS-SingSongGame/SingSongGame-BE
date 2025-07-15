package SingSongGame.BE.common.response;

public enum MessageCode {
    CREATE("200", "생성 성공"),
    GET("200", "조회 성공"),
    UPDATE("200", "수정 성공"),
    DELETE("200", "삭제 성공"),
    LOGIN("200", "로그인 성공"),
    LOGOUT("200", "로그아웃 성공"),
    REISSUE("200", "ACCESS TOKEN 재발급 성공"),
    SUCCESS("200", "요청 성공"),
    ENTER_LOBBY("200", "로비 입장 성공"),
    EXIT_LOBBY("200", "로비 퇴장 성공"),
    FAIL("400", "요청 실패");


    private final String code;
    private final String message;

    MessageCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
