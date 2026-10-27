package util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    /**
     * Names of two cookies an earlier version set so a separate landing page could show the user's name.
     * The landing page is now served by this app, so they are no longer set; they are only cleared, to
     * remove copies left in browsers from before.
     */
    private static final String DISPLAY_COOKIE = "sf_user";
    private static final String USER_ID_COOKIE = "sf_uid";
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

    /** The logged-in user's id, or null for a guest. */
    public static Integer currentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object id = session == null ? null : session.getAttribute(SESSION_USER_ID);
        return id instanceof Integer ? (Integer) id : null;
    }

    /** The logged-in user's role (see model.User), or null for a guest. */
    public static String currentRole(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object role = session == null ? null : session.getAttribute(SESSION_USER_ROLE);
        return role instanceof String ? (String) role : null;
    }

    /** Marks the request's session as logged in as this user, under a fresh session id. */
    public static void logIn(HttpServletRequest request, HttpServletResponse response, User user) {
        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute(SESSION_USER_ID, user.getUserId());
        session.setAttribute(SESSION_USER_NAME, user.getName());
        session.setAttribute(SESSION_USER_ROLE, user.getRole());
        CsrfUtil.rotate(session);
        clearDisplayCookie(request, response);
    }

    /** After the user changes their name: updates the session to match. */
    public static void refreshDisplayName(HttpServletRequest request, HttpServletResponse response, String name) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute(SESSION_USER_NAME, name);
        }
    }

    /** Ends the session, if any. */
    public static void logOut(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        clearDisplayCookie(request, response);
    }

    /** Removes the old display-name cookies, if a browser still has them. */
    public static void clearDisplayCookie(HttpServletRequest request, HttpServletResponse response) {
        response.addCookie(displayCookie(request, DISPLAY_COOKIE, "", 0));
        response.addCookie(displayCookie(request, USER_ID_COOKIE, "", 0));
    }

    private static Cookie displayCookie(HttpServletRequest request, String name, String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        cookie.setHttpOnly(false);
        cookie.setSecure(request.isSecure());
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
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
