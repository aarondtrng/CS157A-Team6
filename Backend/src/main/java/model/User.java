package model;

/**
 * Account row used by login and sign up (User table). Holds the password hash only so AuthService can
 * verify a login; never put this object in a request or session attribute.
 */
public class User {
    // Upper case, matching the User.role column default ('STUDENT') in db/schema.sql.
    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_ADMINISTRATOR = "ADMINISTRATOR";
    /** Accounts no one logs in to, such as the owner of posts imported from Reddit. */
    public static final String ROLE_SYSTEM = "SYSTEM";

    private final int userId;
    private final String name;
    private final String email;
    private final String passwordHash;
    private final String role;
    private final String profileImage;

    public User(int userId, String name, String email, String passwordHash, String role) {
        this(userId, name, email, passwordHash, role, null);
    }

    public User(int userId, String name, String email, String passwordHash, String role, String profileImage) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.profileImage = profileImage;
    }

    /** The stored profile_image value ("uploads/<name>" or a URL), or null when the user has none. */
    public String getProfileImage() {
        return profileImage;
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
