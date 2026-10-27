package util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
     * Name of a small cookie that holds only the user's display name, so the landing page (a separate
     * site on another port, which cannot see our session) can show "Hi, Ada" instead of Log in / Sign up.
     * Browsers share cookies across ports on the same host. It grants no access: every protected page
     * still checks the real session.
     */
    public static final String DISPLAY_COOKIE = "sf_user";
    /** Companion cookie with the user's numeric id, so the landing page can link to their profile. */
    public static final String USER_ID_COOKIE = "sf_uid";
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
        setDisplayCookie(request, response, user.getName());
        response.addCookie(displayCookie(request, USER_ID_COOKIE, Integer.toString(user.getUserId()), -1));
    }

    /** After the user changes their name: updates the session and the "Hi, Name" cookie to match. */
    public static void refreshDisplayName(HttpServletRequest request, HttpServletResponse response, String name) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute(SESSION_USER_NAME, name);
        }
        setDisplayCookie(request, response, name);
    }

    /** Ends the session, if any. */
    public static void logOut(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        clearDisplayCookie(request, response);
    }

    /**
     * Sets the display-name cookie. It must be readable by the landing page's JavaScript, so it is not
     * HttpOnly (the session cookie still is). It lasts until the browser closes and is removed at logout.
     */
    private static void setDisplayCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        String value = URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20");
        response.addCookie(displayCookie(request, DISPLAY_COOKIE, value, -1));
    }

    /** Removes the display-name cookie (also used to drop a stale one after the session has expired). */
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
