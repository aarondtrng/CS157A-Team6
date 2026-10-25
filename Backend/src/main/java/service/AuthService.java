package service;

import dao.UserDao;
import model.User;
import util.PasswordHasher;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Validates login and sign-up input and checks passwords.
 *
 * Passwords arrive already hashed by the browser (webapp/js/auth.js: PBKDF2-SHA256, 64 hex characters),
 * so the server never sees the typed password. That client hash is hashed again here with a random salt
 * before it is stored; otherwise a stolen password_hash column could be replayed to log in. Because the
 * server only sees the hash, password length and confirmation are checked in the browser.
 */
public class AuthService {
    static final String SJSU_DOMAIN = "@sjsu.edu";
    static final int MAX_EMAIL_LENGTH = 255;
    static final int MAX_NAME_LENGTH = 100;
    // Output of js/auth.js: 256-bit PBKDF2 result as lower-case hex.
    static final String CLIENT_HASH_PATTERN = "[0-9a-f]{64}";

    // Checked when the email is unknown, so a failed login takes about as long either way and the
    // response time does not reveal which emails have accounts.
    private static final String DUMMY_HASH = PasswordHasher.hash("0".repeat(64).toCharArray());

    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    /** Trims and lower-cases an email; empty string when null. */
    public String normalizeEmail(String email) {
        return email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
    }

    /** Trims a display name; empty string when null. */
    public String normalizeName(String name) {
        return name == null ? "" : name.strip();
    }

    /**
     * Checks sign-up input. Expects values already passed through normalizeName / normalizeEmail.
     *
     * @return user-facing error messages; empty when the input is valid
     */
    public List<String> validateSignup(String name, String email, String clientHash) {
        List<String> errors = new ArrayList<>();
        if (name.isEmpty()) {
            errors.add("Enter your name.");
        } else if (name.length() > MAX_NAME_LENGTH) {
            errors.add("Name must be at most " + MAX_NAME_LENGTH + " characters.");
        }
        if (!isSjsuEmail(email)) {
            errors.add("Use your SJSU email address (ending in " + SJSU_DOMAIN + ").");
        }
        if (!isClientHash(clientHash)) {
            errors.add("Your password could not be processed. Make sure JavaScript is enabled and try again.");
        }
        return errors;
    }

    /**
     * Creates a student account from already-validated input.
     *
     * @return the new user_id, or Optional.empty() when the email is already registered
     * @throws UnsupportedOperationException until the UserDao SQL is implemented
     */
    public Optional<Integer> signUp(String name, String email, String clientHash) throws SQLException {
        return userDao.insertStudent(name, email, PasswordHasher.hash(clientHash.toCharArray()));
    }

    /**
     * Checks an email and password.
     *
     * @return the account when both match; Optional.empty() for an unknown email or wrong password
     *         (callers must show the same message for both)
     * @throws UnsupportedOperationException until the UserDao SQL is implemented
     */
    public Optional<User> logIn(String email, String clientHash) throws SQLException {
        if (email.isEmpty() || !isClientHash(clientHash)) {
            return Optional.empty();
        }
        Optional<User> user = userDao.findByEmail(email);
        if (user.isEmpty()) {
            PasswordHasher.verify(clientHash.toCharArray(), DUMMY_HASH);
            return Optional.empty();
        }
        return PasswordHasher.verify(clientHash.toCharArray(), user.get().getPasswordHash()) ? user : Optional.empty();
    }

    boolean isClientHash(String value) {
        return value != null && value.matches(CLIENT_HASH_PATTERN);
    }

    boolean isSjsuEmail(String email) {
        return email.length() <= MAX_EMAIL_LENGTH
                && email.endsWith(SJSU_DOMAIN)
                && email.length() > SJSU_DOMAIN.length()
                && email.indexOf('@') == email.length() - SJSU_DOMAIN.length()
                && email.chars().noneMatch(Character::isWhitespace);
    }
}
