package SingSongGame.BE.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtil {

    private static final int REFRESH_TOKEN_EXPIRATION_SECONDS = 14 * 24 * 60 * 60;

    public static void addSameSiteCookie(HttpServletResponse response, String name, String value, boolean secure) {
        StringBuilder cookie = new StringBuilder();
        cookie.append(name).append("=").append(value).append(";");
        cookie.append(" Path=/;");
        cookie.append(" Max-Age=").append(REFRESH_TOKEN_EXPIRATION_SECONDS).append(";");
        cookie.append(" HttpOnly;");
        if (secure) {
            cookie.append(" Secure;");
            cookie.append(" SameSite=None;"); // ✅ 배포용
        } else {
            cookie.append(" SameSite=Lax;"); // ✅ 로컬에서는 이게 안전함
        }

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public static void deleteSameSiteCookie(HttpServletResponse response, String name, boolean secure) {
        String[] sameSiteValues = { "None", "Lax", "Strict" };

        for (String sameSite : sameSiteValues) {
            StringBuilder cookie = new StringBuilder();
            cookie.append(name).append("=;");
            cookie.append(" Path=/;");
            cookie.append(" Max-Age=0;");
            cookie.append(" HttpOnly;");
            if (secure) {
                cookie.append(" Secure;");
            }
            cookie.append(" SameSite=").append(sameSite).append(";");
            response.addHeader("Set-Cookie", cookie.toString());
        }
    }

    public static void expireCookieWithSameSite(HttpServletResponse response, String name, boolean secure) {
        StringBuilder cookie = new StringBuilder();
        cookie.append(name).append("=;");
        cookie.append(" Path=/;");
        cookie.append(" Max-Age=0;");
        cookie.append(" HttpOnly;");
        if (secure) {
            cookie.append(" Secure;");
        }
        cookie.append(" SameSite=None;");
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
