package util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import model.User;

/**
 * Session-based login checks shared by filters and servlets.
 *
 * Logging in stores the user's id, name, and role in the session and replaces the session id (prevents
 * session fixation). The password hash never goes into the session.
 */
public final class AuthUtil {
    public static final String SESSION_USER_ID = "userId";
    public static final String SESSION_USER_NAME = "userName";
    public static final String SESSION_USER_ROLE = "userRole";
    public static final String LOGIN_PATH = "/login";
    public static final String SIGNUP_PATH = "/signup";
    /** Where users land after login or sign up when no valid "next" was given. */
    public static final String DEFAULT_AFTER_LOGIN = "/items";

    private AuthUtil() {
    }

    public static boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(SESSION_USER_ID) != null;
    }

    /** Marks the request's session as logged in as this user, under a fresh session id. */
    public static void logIn(HttpServletRequest request, User user) {
        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute(SESSION_USER_ID, user.getUserId());
        session.setAttribute(SESSION_USER_NAME, user.getName());
        session.setAttribute(SESSION_USER_ROLE, user.getRole());
    }

    /** Ends the session, if any. */
    public static void logOut(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    /** The requested path (without the context path) plus query string, for use as a post-login return target. */
    public static String currentPathWithQuery(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String query = request.getQueryString();
        return query == null ? path : path + "?" + query;
    }

    /**
     * Returns next when it is a path inside this app, otherwise DEFAULT_AFTER_LOGIN. Rejects absolute URLs,
     * protocol-relative "//host" and "/\host" forms, and control characters, so login cannot be used to
     * redirect to another site.
     */
    public static String safeNext(String next) {
        if (next == null || next.isEmpty() || next.length() > 2000
                || next.charAt(0) != '/'
                || next.startsWith("//")
                || next.indexOf('\\') >= 0
                || next.chars().anyMatch(ch -> ch < 0x20 || ch == 0x7f)) {
            return DEFAULT_AFTER_LOGIN;
        }
        return next;
    }
}
