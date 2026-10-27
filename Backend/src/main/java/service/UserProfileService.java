package service;

import dao.UserDao;
import model.UserProfile;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Loads profiles and validates profile edits. */
public class UserProfileService {
    static final int MAX_NAME_LENGTH = 100;
    static final int MAX_DESCRIPTION_LENGTH = 1000;

    private final UserDao userDao;

    public UserProfileService(UserDao userDao) {
        this.userDao = userDao;
    }

    public Optional<UserProfile> getProfile(int userId) throws SQLException {
        return userDao.findProfileById(userId);
    }

    public boolean updateProfile(
            int userId,
            String name,
            String phoneNumber,
            String profileDescription,
            String profileImage) throws SQLException {

        return userDao.updateProfile(userId, name, phoneNumber, profileDescription, profileImage);
    }

    /** Trims a name and collapses runs of spaces to one; empty string when null. */
    public String normalizeName(String name) {
        return name == null ? "" : name.strip().replaceAll("\\s+", " ");
    }

    /** Trims a phone number or description; empty string when null. */
    public String normalizeText(String text) {
        return text == null ? "" : text.strip();
    }

    /**
     * Puts a phone number in the one stored form, (408) 555-0123, when it is a 10-digit US number (a leading
     * 1 or +1 is dropped). Anything else is returned trimmed and unchanged, so validate() can reject it and
     * the form can show what the student typed.
     */
    public String normalizePhone(String phoneNumber) {
        String text = normalizeText(phoneNumber);
        if (text.isEmpty() || !text.matches("[0-9+()\\-. ]*")) {
            return text;
        }
        String digits = text.replaceAll("\\D", "");
        if (digits.length() == 11 && digits.charAt(0) == '1') {
            digits = digits.substring(1);
        }
        if (digits.length() != 10) {
            return text;
        }
        return "(" + digits.substring(0, 3) + ") " + digits.substring(3, 6) + "-" + digits.substring(6);
    }

    /**
     * Checks an edit. Expects values already passed through normalizeName / normalizeText. Keys of the
     * returned map are the field names the form shows errors under: name, phoneNumber, profileDescription.
     * Empty when valid.
     */
    public Map<String, String> validate(String name, String phoneNumber, String profileDescription) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (name.isEmpty()) {
            errors.put("name", "Enter your name.");
        } else if (name.length() > MAX_NAME_LENGTH) {
            errors.put("name", "Name must be at most " + MAX_NAME_LENGTH + " characters.");
        } else if (hasControlCharacter(name)) {
            errors.put("name", "Name can't contain control characters.");
        }

        // Empty is allowed (the phone number is optional). Otherwise it must already be in the form
        // (408) 555-0123, which normalizePhone produces from any 10-digit US number.
        if (!phoneNumber.isEmpty() && !phoneNumber.matches("\\(\\d{3}\\) \\d{3}-\\d{4}")) {
            errors.put("phoneNumber", "Enter a 10-digit phone number, like (408) 555-0123.");
        }

        if (profileDescription.length() > MAX_DESCRIPTION_LENGTH) {
            errors.put("profileDescription",
                    "Description must be at most " + MAX_DESCRIPTION_LENGTH + " characters.");
        }
        return errors;
    }

    private static boolean hasControlCharacter(String text) {
        return text.chars().anyMatch(ch -> ch < 0x20 || ch == 0x7f);
    }
}
