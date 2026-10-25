package model;

/**
 * Account row used by login and sign up (User table). Holds the password hash only so AuthService can
 * verify a login; never put this object in a request or session attribute.
 */
public class User {
    public static final String ROLE_STUDENT = "student";
    public static final String ROLE_ADMINISTRATOR = "administrator";

    private final int userId;
    private final String name;
    private final String email;
    private final String passwordHash;
    private final String role;

    public User(int userId, String name, String email, String passwordHash, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    /** student or administrator (guests have no row). */
    public String getRole() {
        return role;
    }
}
