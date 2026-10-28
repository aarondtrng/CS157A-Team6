package dao;

import config.DatabaseConfig;
import model.User;
import model.UserProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.Optional;

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

    private static final String FIND_PROFILE_BY_ID_SQL = 
        "SELECT user_id, name, email, phone_number, profile_description, profile_image, role " +
        "FROM User WHERE user_id = ?";
    
    private static final String UPDATE_PROFILE_SQL =
        "UPDATE User SET name = ?, phone_number = ?, " +
        "profile_description = ?, profile_image = ? " +
        "WHERE user_id = ?";

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
    private static void requireSql(String sql, String method) {
        if (sql.isBlank()) {
            throw new UnsupportedOperationException("UserDao." + method + ": SQL not implemented yet");
        }
    }
}
