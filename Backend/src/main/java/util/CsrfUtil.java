package util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cross-site request forgery protection for POST forms.
 *
 * Each session holds one random token. Pages put it in a hidden field named {@link #PARAM}, and every
 * servlet that handles a POST calls {@link #isValid} before doing anything. Another site cannot read the
 * token, so a form it submits on a student's behalf is rejected. AuthUtil.logIn rotates the token, so the
 * value a guest saw before logging in is not valid afterwards.
 */
public final class CsrfUtil {
    public static final String PARAM = "csrfToken";
    private static final String SESSION_KEY = "csrfToken";
    private static final int TOKEN_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfUtil() {
    }

    /** The session's token, creating the session and token on first use. For rendering into a form. */
    public static String token(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Object existing = session.getAttribute(SESSION_KEY);
        return existing instanceof String ? (String) existing : rotate(session);
    }

    /** Replaces the session's token with a fresh random one and returns it. */
    public static String rotate(HttpSession session) {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute(SESSION_KEY, token);
        return token;
    }

    /** True when the request carries the session's token. False when there is no session or no token. */
    public static boolean isValid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object expected = session.getAttribute(SESSION_KEY);
        String submitted = request.getParameter(PARAM);
        if (!(expected instanceof String) || submitted == null) {
            return false;
        }
        return MessageDigest.isEqual(
                ((String) expected).getBytes(StandardCharsets.UTF_8),
                submitted.getBytes(StandardCharsets.UTF_8));
    }
}
