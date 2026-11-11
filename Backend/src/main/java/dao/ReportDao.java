package dao;

import config.DatabaseConfig;
import model.ReportRow;
import model.ReportTarget;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** All five report SQL placeholders are intentionally blank. See db/REPORT_QUERIES.md. */
public class ReportDao {
    // TODO SQL R1: public item and its owner. Parameters: item id, REMOVED, APPROVED.
    private static final String FIND_ITEM_SQL = """
        SELECT i.item_id, i.user_id, i.title FROM Item i
        WHERE i.item_id = ?
            AND i.status <> ?
            AND NOT EXISTS (
                    SELECT * FROM RedditItem r WHERE r.item_id = i.item_id AND r.approval_status <> ?
                    )
        """;
    // TODO SQL R2: student profile. Parameters: user id, STUDENT.
    private static final String FIND_PROFILE_SQL = "SELECT user_id, name FROM User WHERE user_id = ? AND role = ?";
    // TODO SQL R3: new report. Parameters: reporter id, nullable item id, reported user id, reason, PENDING.
    private static final String CREATE_REPORT_SQL = "INSERT INTO Report (reported_by_user_id, item_id, reported_user_id, reason, status)" +
                                                    "VALUES (?, ?, ?, ?, ?)";
    // TODO SQL R4: administrator queue. Parameters: filter, filter (ALL or a status).
    private static final String LIST_REPORTS_SQL = """
        SELECT r.report_id,
               r.reported_by_user_id AS reporter_id,
               reporter.name AS reporter_name,
               r.item_id,
               r.reported_user_id,
               reported.name AS reported_user_name,
               r.reason,
               r.status,
               r.report_date
            FROM Report r, User reporter, User reported
            WHERE r.reported_by_user_id = reporter.user_id
                AND r.reported_user_id = reported.user_id
            AND (? = 'ALL' OR r.status = ?)
                ORDER BY r.report_date DESC, r.report_id DESC
        """;
    // TODO SQL R5: pending-only status update. Parameters: new status, report id, PENDING.
    private static final String UPDATE_STATUS_SQL = "UPDATE Report SET status = ? WHERE report_id = ? AND status = ?";

    /** A separate failure lets the UI show an honest unavailable state before SQL is supplied. */
    public static class SqlNotConfiguredException extends DataAccessException {
        public SqlNotConfiguredException(String name) {
            super("Fill ReportDao." + name + " using db/REPORT_QUERIES.md", null);
        }
    }

    private static String required(String sql, String name) {
        if (sql.isBlank()) { throw new SqlNotConfiguredException(name); }
        return sql;
    }

    public Optional<ReportTarget> findItem(int itemId) {
        String sql = required(FIND_ITEM_SQL, "FIND_ITEM_SQL");
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, itemId);
            statement.setString(2, "REMOVED");
            statement.setString(3, "APPROVED");
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(new ReportTarget(rows.getInt("item_id"),
                        rows.getInt("user_id"), rows.getString("title"))) : Optional.empty();
            }
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("ReportDao.findItem failed", e);
        }
    }

    public Optional<ReportTarget> findProfile(int userId) {
        String sql = required(FIND_PROFILE_SQL, "FIND_PROFILE_SQL");
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setString(2, "STUDENT");
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(new ReportTarget(null,
                        rows.getInt("user_id"), rows.getString("name"))) : Optional.empty();
            }
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("ReportDao.findProfile failed", e);
        }
    }

    public boolean create(int reporterId, ReportTarget target, String reason) {
        String sql = required(CREATE_REPORT_SQL, "CREATE_REPORT_SQL");
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reporterId);
            if (target.getItemId() == null) { statement.setNull(2, Types.INTEGER); }
            else { statement.setInt(2, target.getItemId()); }
            statement.setInt(3, target.getUserId());
            statement.setString(4, reason);
            statement.setString(5, "PENDING");
            return statement.executeUpdate() == 1;
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("ReportDao.create failed", e);
        }
    }

    public List<ReportRow> findReports(String filter) {
        String sql = required(LIST_REPORTS_SQL, "LIST_REPORTS_SQL");
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, filter);
            statement.setString(2, filter);
            // JDBC bounds the list; no vendor-specific SQL pagination is needed.
            statement.setMaxRows(100);
            List<ReportRow> result = new ArrayList<>();
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    result.add(new ReportRow(rows.getInt("report_id"), rows.getInt("reporter_id"),
                            rows.getString("reporter_name"), rows.getObject("item_id", Integer.class),
                            rows.getInt("reported_user_id"),
                            rows.getString("reported_user_name"), rows.getString("reason"),
                            rows.getString("status"), rows.getObject("report_date", LocalDateTime.class)));
                }
            }
            return result;
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("ReportDao.findReports failed", e);
        }
    }

    public boolean updateStatus(int reportId, String status) {
        String sql = required(UPDATE_STATUS_SQL, "UPDATE_STATUS_SQL");
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, reportId);
            statement.setString(3, "PENDING");
            return statement.executeUpdate() == 1;
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("ReportDao.updateStatus failed", e);
        }
    }
}
