package model;

/**
 * Account row used by login and sign up (User table). Holds the password hash only so AuthService can
 * verify a login; never put this object in a request or session attribute.
 */
public class User {
    // Upper case, matching the User.role column default ('STUDENT') in db/schema.sql.
    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_ADMINISTRATOR = "ADMINISTRATOR";

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

    /** STUDENT or ADMINISTRATOR (guests have no row). */
    public String getRole() {
        return role;
    }
}
