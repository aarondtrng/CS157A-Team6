package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import config.DatabaseConfig;
import model.User;
import model.UserProfile;

/** Database access for user accounts and profiles. */
public class UserDao {

    private static final String FIND_BY_EMAIL_SQL = "SELECT user_id, name, email, password_hash, role FROM User WHERE email = ?";

    private static final String INSERT_STUDENT_SQL = "INSERT INTO User (name, email, password_hash, role) VALUES (?, ?, ?, ?)";

    /** Looks up an account by email, or empty if there isn't one. */
    public Optional<User> findByEmail(String email) throws SQLException {
        requireSql(FIND_BY_EMAIL_SQL, "findByEmail");
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_EMAIL_SQL)) {
            statement.setString(1, email);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new User(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("role")));
            }
        }
    }

    /** Creates a student account and returns its id, or empty if the email is already taken. */
    public Optional<Integer> insertStudent(String name, String email, String passwordHash) throws SQLException {
        requireSql(INSERT_STUDENT_SQL, "insertStudent");
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     INSERT_STUDENT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            statement.setString(4, User.ROLE_STUDENT);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Insert into User returned no generated user_id");
                }
                return Optional.of(keys.getInt(1));
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            // UNIQUE(email) rejected the row: the email is already registered.
            return Optional.empty();
        }
    }

    /**
     * The user_id of the account with this email.
     * Column the code reads: user_id
     * ? 1 = email
     */
    private static final String FIND_USER_ID_BY_EMAIL_SQL = """
        SELECT user_id FROM User WHERE email = ? 
    """;

    /**
     * Create the Reddit import account (user_id and created_at use their defaults).
     * ? 1 = name, 2 = email, 3 = password_hash, 4 = role
     */
    private static final String INSERT_SYSTEM_USER_SQL = """
    INSERT INTO User (name, email, password_hash, role)
    VALUES (?, ?, ?, ?)
    """;

    /** The account that owns posts imported from r/SJSU (Item.user_id is required). */
    public static final String REDDIT_BOT_EMAIL = "reddit-bot@spartanfinds.local";
    public static final String REDDIT_BOT_NAME = "r/SJSU (Reddit)";
    /** Not a valid PasswordHasher hash, so no password can ever log in to the account. */
    private static final String UNUSABLE_PASSWORD_HASH = "!";

    /**
     * Returns the user_id of the Reddit import account, creating it the first time. Its role is SYSTEM,
     * so it has none of a student's or an administrator's permissions.
     */
    public int ensureRedditBotUser() throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection()) {
            Optional<Integer> existing = findUserIdByEmail(connection, REDDIT_BOT_EMAIL);
            if (existing.isPresent()) {
                return existing.get();
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    INSERT_SYSTEM_USER_SQL, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, REDDIT_BOT_NAME);
                statement.setString(2, REDDIT_BOT_EMAIL);
                statement.setString(3, UNUSABLE_PASSWORD_HASH);
                statement.setString(4, User.ROLE_SYSTEM);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Insert into User returned no generated user_id");
                    }
                    return keys.getInt(1);
                }
            } catch (SQLIntegrityConstraintViolationException e) {
                // Another request created it at the same moment (UNIQUE email); use that row.
                return findUserIdByEmail(connection, REDDIT_BOT_EMAIL).orElseThrow(() -> e);
            }
        }
    }

    private static Optional<Integer> findUserIdByEmail(Connection connection, String email) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_USER_ID_BY_EMAIL_SQL)) {
            statement.setString(1, email);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(rs.getInt("user_id")) : Optional.empty();
            }
        }
    }

    private static final String FIND_PROFILE_BY_ID_SQL = 
        "SELECT user_id, name, email, phone_number, profile_description, profile_image, role " +
        "FROM User WHERE user_id = ?";
    
    private static final String UPDATE_PROFILE_SQL =
        "UPDATE User SET name = ?, phone_number = ?, " +
        "profile_description = ?, profile_image = ? " +
        "WHERE user_id = ?";

    private static final String DELETE_PROFILE_SQL =
        "DELETE FROM User WHERE user_id = ?";

    private static final String FIND_PROFILES_BY_NAME_SQL =
        "SELECT user_id, name, email, phone_number, profile_description, profile_image, role " +
        "FROM User WHERE name LIKE ?";

    public Optional<UserProfile> findProfileById(int userId) throws SQLException {
    requireSql(FIND_PROFILE_BY_ID_SQL, "findProfileById");

    try (Connection connection = DatabaseConfig.getConnection();
         PreparedStatement statement = connection.prepareStatement(FIND_PROFILE_BY_ID_SQL)) {

        statement.setInt(1, userId);

        try (ResultSet rs = statement.executeQuery()) {
            if (!rs.next()) {
                return Optional.empty();
            }

            return Optional.of(new UserProfile(
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone_number"),
                    rs.getString("profile_description"),
                    rs.getString("profile_image"),
                    rs.getString("role")));
        }
    }
}

public List<UserProfile> findProfilesByName(String name) throws SQLException {
    requireSql(FIND_PROFILES_BY_NAME_SQL, "findProfilesByName");

    List<UserProfile> profiles = new ArrayList<>();

    try (Connection connection = DatabaseConfig.getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(FIND_PROFILES_BY_NAME_SQL)) {

        statement.setString(1, "%" + name + "%");

        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                profiles.add(new UserProfile(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone_number"),
                        rs.getString("profile_description"),
                        rs.getString("profile_image"),
                        rs.getString("role")));
            }
        }
    }

    return profiles;
}

    public boolean updateProfile(
        int userId,
        String name,
        String phoneNumber,
        String profileDescription,
        String profileImage) throws SQLException {

    requireSql(UPDATE_PROFILE_SQL, "updateProfile");

    try (Connection connection = DatabaseConfig.getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(UPDATE_PROFILE_SQL)) {

        statement.setString(1, name);
        statement.setString(2, phoneNumber);
        statement.setString(3, profileDescription);
        statement.setString(4, profileImage);
        statement.setInt(5, userId);

        return statement.executeUpdate() == 1;
    }
}

    public boolean deleteProfile(int userId) throws SQLException {
    requireSql(DELETE_PROFILE_SQL, "deleteProfile");

    try (Connection connection = DatabaseConfig.getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(DELETE_PROFILE_SQL)) {

        statement.setInt(1, userId);
        return statement.executeUpdate() == 1;
        }
    }

    private static void requireSql(String sql, String method) {
        if (sql.isBlank()) {
            throw new UnsupportedOperationException("UserDao." + method + ": SQL not implemented yet");
        }
    }
}
