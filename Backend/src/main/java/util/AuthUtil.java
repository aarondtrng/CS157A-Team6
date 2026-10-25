package util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Session-based login checks shared by filters and servlets.
 *
 * The login feature (not built yet) must store the student's user_id in the session under
 * SESSION_USER_ID and replace the session id on login. Until then nobody counts as logged in.
 */
public final class AuthUtil {
    public static final String SESSION_USER_ID = "userId";
    public static final String LOGIN_PATH = "/login";

    private AuthUtil() {
    }

    public static boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(SESSION_USER_ID) != null;
    }

    /** The requested path (without the context path) plus query string, for use as a post-login return target. */
    public static String currentPathWithQuery(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String query = request.getQueryString();
        return query == null ? path : path + "?" + query;
    }
}
