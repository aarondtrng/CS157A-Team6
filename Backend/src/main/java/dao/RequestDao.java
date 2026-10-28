package dao;

import config.DatabaseConfig;
import model.RequestRow;
import model.RequestStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Database access for ownership requests. */
public class RequestDao {
    /** Item status values we picked ourselves; the schema doesn't define them yet. */
    public static final String ITEM_ACTIVE = "ACTIVE";
    static final String ITEM_RETURNED = "RETURNED";

    private static final String TYPE_FOUND = "FOUND";

    /** Each request list shows at most this many of the newest requests, so the page stays bounded. */
    static final int MAX_ROWS = 100;

    /** Adds a pending request, but only if the item can be requested and the student has no active request for it. */
    public boolean createRequest(int requesterId, int itemId, String description) {
        String sql = "INSERT INTO ItemRequest (requester_id, item_id, request_description, status)"
                + " SELECT ?, i.item_id, ?, ?"
                + " FROM Item i"
                + " WHERE i.item_id = ? AND i.type = ? AND i.status = ? AND i.user_id <> ?"
                + " AND NOT EXISTS (SELECT * FROM RedditItem ri WHERE ri.item_id = i.item_id)"
                + " AND NOT EXISTS (SELECT * FROM ItemRequest r"
                + " WHERE r.requester_id = ? AND r.item_id = i.item_id AND r.status IN (?, ?))";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            statement.setInt(index++, requesterId);
            statement.setString(index++, description);
            statement.setString(index++, RequestStatus.PENDING);
            statement.setInt(index++, itemId);
            statement.setString(index++, TYPE_FOUND);
            statement.setString(index++, ITEM_ACTIVE);
            statement.setInt(index++, requesterId);
            statement.setInt(index++, requesterId);
            statement.setString(index++, RequestStatus.PENDING);
            statement.setString(index, RequestStatus.ACCEPTED);
            return statement.executeUpdate() == 1;
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("RequestDao.createRequest failed", e);
        }
    }

    /** Returns the status of the student's active request for this item, if they have one. */
    public Optional<String> findActiveRequestStatus(int requesterId, int itemId) {
        String sql = "SELECT status FROM ItemRequest"
                + " WHERE requester_id = ? AND item_id = ? AND status IN (?, ?)";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, requesterId);
            statement.setInt(2, itemId);
            statement.setString(3, RequestStatus.PENDING);
            statement.setString(4, RequestStatus.ACCEPTED);
            try (ResultSet row = statement.executeQuery()) {
                return row.next() ? Optional.of(row.getString("status")) : Optional.empty();
            }
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("RequestDao.findActiveRequestStatus failed", e);
        }
    }

    /** Gets the requests this student has made, newest first. */
    public List<RequestRow> findRequestsMadeBy(int requesterId) {
        String sql = "SELECT r.request_id, r.item_id, i.title, u.user_id, u.name,"
                + " r.request_description, r.status, r.created_at"
                + " FROM ItemRequest r"
                + " JOIN Item i ON i.item_id = r.item_id"
                + " JOIN User u ON u.user_id = i.user_id"
                + " WHERE r.requester_id = ?"
                + " ORDER BY r.created_at DESC, r.request_id DESC";
        return queryRows(sql, requesterId, "RequestDao.findRequestsMadeBy failed");
    }

    /** Gets the requests other students made on this student's posts, newest first. */
    public List<RequestRow> findRequestsOnPostsOf(int posterId) {
        String sql = "SELECT r.request_id, r.item_id, i.title, u.user_id, u.name,"
                + " r.request_description, r.status, r.created_at"
                + " FROM ItemRequest r"
                + " JOIN Item i ON i.item_id = r.item_id"
                + " JOIN User u ON u.user_id = r.requester_id"
                + " WHERE i.user_id = ?"
                + " ORDER BY r.created_at DESC, r.request_id DESC";
        return queryRows(sql, posterId, "RequestDao.findRequestsOnPostsOf failed");
    }

    /** Lets the post's owner change a request's status; returns false if they aren't allowed to. */
    public boolean changeStatusAsPoster(int requestId, int posterId, String newStatus, String... allowedCurrent) {
        String sql = "UPDATE ItemRequest SET status = ?"
                + " WHERE request_id = ? AND status IN (" + placeholders(allowedCurrent.length) + ")"
                + " AND item_id IN (SELECT item_id FROM Item WHERE user_id = ?)";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            statement.setString(index++, newStatus);
            statement.setInt(index++, requestId);
            for (String status : allowedCurrent) {
                statement.setString(index++, status);
            }
            statement.setInt(index, posterId);
            return statement.executeUpdate() == 1;
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("RequestDao.changeStatusAsPoster failed", e);
        }
    }

    /** Marks the request completed and the item returned together, so one can't happen without the other. */
    public boolean completeRequest(int requestId, int posterId) {
        String requestSql = "UPDATE ItemRequest SET status = ?"
                + " WHERE request_id = ? AND status = ?"
                + " AND item_id IN (SELECT item_id FROM Item WHERE user_id = ?)";
        String itemSql = "UPDATE Item SET status = ?"
                + " WHERE user_id = ? AND status = ?"
                + " AND item_id IN (SELECT item_id FROM ItemRequest WHERE request_id = ?)";

        try (Connection connection = DatabaseConfig.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int requestsChanged;
                try (PreparedStatement statement = connection.prepareStatement(requestSql)) {
                    statement.setString(1, RequestStatus.COMPLETED);
                    statement.setInt(2, requestId);
                    statement.setString(3, RequestStatus.ACCEPTED);
                    statement.setInt(4, posterId);
                    requestsChanged = statement.executeUpdate();
                }
                int itemsChanged = 0;
                if (requestsChanged == 1) {
                    try (PreparedStatement statement = connection.prepareStatement(itemSql)) {
                        statement.setString(1, ITEM_RETURNED);
                        statement.setInt(2, posterId);
                        statement.setString(3, ITEM_ACTIVE);
                        statement.setInt(4, requestId);
                        itemsChanged = statement.executeUpdate();
                    }
                }
                if (requestsChanged == 1 && itemsChanged == 1) {
                    connection.commit();
                    return true;
                }
                connection.rollback();
                return false;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("RequestDao.completeRequest failed", e);
        }
    }

    /** Lets a student cancel their own pending or accepted request. */
    public boolean cancelRequest(int requestId, int requesterId) {
        String sql = "UPDATE ItemRequest SET status = ?"
                + " WHERE request_id = ? AND requester_id = ? AND status IN (?, ?)";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, RequestStatus.CANCELLED);
            statement.setInt(2, requestId);
            statement.setInt(3, requesterId);
            statement.setString(4, RequestStatus.PENDING);
            statement.setString(5, RequestStatus.ACCEPTED);
            return statement.executeUpdate() == 1;
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("RequestDao.cancelRequest failed", e);
        }
    }

    private static List<RequestRow> queryRows(String sql, int userId, String failureMessage) {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setMaxRows(MAX_ROWS);

            List<RequestRow> rows = new ArrayList<>();
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(new RequestRow(
                            result.getInt("request_id"),
                            result.getInt("item_id"),
                            result.getString("title"),
                            result.getInt("user_id"),
                            result.getString("name"),
                            result.getString("request_description"),
                            result.getString("status"),
                            result.getObject("created_at", LocalDateTime.class)));
                }
            }
            return rows;
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException(failureMessage, e);
        }
    }

    /** "?, ?, ?" with the given count: fixed text, so the statement never contains request data. */
    private static String placeholders(int count) {
        return "?, ".repeat(count - 1) + "?";
    }
}
