package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import config.DatabaseConfig;
import dto.RedditPost;
import model.PendingRedditItem;

/**
 * Database access for posts imported from r/SJSU. A RedditItem row is a specialized Item (ISA): it shares
 * the Item's item_id and records the Reddit link and whether an administrator has approved it.
 * Every value is bound with a ? placeholder.
 */
public class RedditItemDao {
    public static final String PENDING = "PENDING";
    public static final String APPROVED = ItemDao.APPROVAL_APPROVED;
    public static final String REJECTED = "REJECTED";

    /**
     * Has this Reddit thread been imported before (in any approval state)?
     * Return one number: how many RedditItem rows have this post_url.
     * ? 1 = post_url
     */
    private static final String COUNT_BY_POST_URL_SQL = "SELECT COUNT(*) FROM RedditItem WHERE post_url = ?";

    /**
     * Add the Item row for an imported post (Item.status uses its default).
     * ? 1 = user_id, 2 = category_id, 3 = title, 4 = description, 5 = type, 6 = created_at
     */
    private static final String INSERT_ITEM_SQL = """
    INSERT INTO Item (user_id, category_id, title, description, type, created_at)
        VALUES (?, ?, ?, ?, ?, ?)
    """;

    /**
     * Add one photo of the item.
     * ? 1 = item_id, 2 = image_url, 3 = display_order
     */
    private static final String INSERT_IMAGE_SQL = """
    INSERT INTO ItemImage (item_id, image_url, display_order)
        VALUES (?, ?, ?)
    """;

    /**
     * Mark the new Item as a Reddit import waiting for approval.
     * ? 1 = item_id, 2 = post_url, 3 = approval_status ('PENDING')
     */
    private static final String INSERT_REDDIT_ITEM_SQL = """
    INSERT INTO RedditItem (item_id, post_url, approval_status)
        VALUES (?, ?, ?)
    """;

    /**
     * Imported posts in one approval state for the admin dashboard, newest first (ties: higher item_id
     * first). Leave out items whose Item.status is the removed status. Join Item, Category and RedditItem.
     * Columns the code reads: item_id, title, description, type, created_at, category_name, post_url,
     * approval_status, thumbnail_url. For thumbnail_url you can add ItemDao.THUMBNAIL to the column list
     * (it is a subquery that ends with "AS thumbnail_url"), e.g. "SELECT ..., " + ItemDao.THUMBNAIL + " FROM ...".
     * ? 1 = approval_status, 2 = the removed status ('REMOVED')
     */
    private static final String FIND_BY_STATUS_SQL = """
    SELECT i.item_id, i.title, i.description, i.type, i.created_at, c.category_name, r.post_url, r.approval_status, """ + ItemDao.THUMBNAIL + """
        FROM Item i JOIN Category c ON  c.category_id = i.category_id
        JOIN RedditItem r ON r.item_id = i.item_id
        WHERE r.approval_status = ? AND i.status <> ?
        ORDER BY i.created_at DESC, i.item_id DESC
    """;

    /**
     * How many imported posts are in each approval state, one row per state.
     * Columns the code reads: approval_status, total
     * (no ? values)
     */
    private static final String COUNT_BY_STATUS_SQL = """
    SELECT approval_status, COUNT(*) AS total FROM RedditItem GROUP BY approval_status
    """;

    /**
     * Approve or reject a post, but only if it is still waiting (so a second click changes nothing).
     * ? 1 = new approval_status, 2 = item_id, 3 = current approval_status ('PENDING')
     */
    private static final String SET_APPROVAL_SQL = """
    UPDATE RedditItem SET approval_status = ? WHERE item_id = ? AND approval_status = ?
    """;

    /**
     * The Reddit link of one item; no row means the item was posted on SpartanFinds.
     * Column the code reads: post_url
     * ? 1 = item_id
     */
    private static final String FIND_POST_URL_SQL = """
    SELECT post_url FROM RedditItem WHERE item_id = ?
    """;

    public int countByPostUrl(String postUrl) {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(COUNT_BY_POST_URL_SQL)) {
            statement.setString(1, postUrl);
            try (ResultSet row = statement.executeQuery()) {
                return row.next() ? row.getInt(1) : 0;
            }
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("RedditItemDao.countByPostUrl failed", e);
        }
    }

    /**
     * Saves the Item, its photos and its RedditItem row (PENDING) in one transaction, so a post is never
     * half imported. Returns the new item_id.
     */
    public int createPendingItem(int ownerUserId, RedditPost post, int categoryId, List<String> imageUrls) {
        try (Connection connection = DatabaseConfig.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int itemId;
                try (PreparedStatement statement =
                             connection.prepareStatement(INSERT_ITEM_SQL, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, ownerUserId);
                    statement.setInt(2, categoryId);
                    statement.setString(3, post.title());
                    statement.setString(4, post.description());
                    statement.setString(5, post.type());
                    statement.setObject(6, post.createdAt());
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Insert into Item returned no generated item_id");
                        }
                        itemId = keys.getInt(1);
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(INSERT_IMAGE_SQL)) {
                    int displayOrder = 1;
                    for (String imageUrl : imageUrls) {
                        statement.setInt(1, itemId);
                        statement.setString(2, imageUrl);
                        statement.setInt(3, displayOrder++);
                        statement.executeUpdate();
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(INSERT_REDDIT_ITEM_SQL)) {
                    statement.setInt(1, itemId);
                    statement.setString(2, post.postUrl());
                    statement.setString(3, PENDING);
                    statement.executeUpdate();
                }

                connection.commit();
                return itemId;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("RedditItemDao.createPendingItem failed", e);
        }
    }

    public List<PendingRedditItem> findByStatus(String approvalStatus, int maxRows) {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_STATUS_SQL)) {
            statement.setString(1, approvalStatus);
            statement.setString(2, ItemDao.STATUS_REMOVED);
            statement.setMaxRows(maxRows);

            List<PendingRedditItem> items = new ArrayList<>();
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    items.add(new PendingRedditItem(
                            rows.getInt("item_id"),
                            rows.getString("title"),
                            rows.getString("description"),
                            rows.getString("type"),
                            rows.getString("category_name"),
                            rows.getObject("created_at", LocalDateTime.class),
                            rows.getString("post_url"),
                            rows.getString("approval_status"),
                            rows.getString("thumbnail_url")));
                }
            }
            return items;
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("RedditItemDao.findByStatus failed", e);
        }
    }

    /** approval_status → number of imported posts, e.g. {PENDING=3, APPROVED=12}. Missing states mean 0. */
    public Map<String, Integer> countByStatus() {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(COUNT_BY_STATUS_SQL);
             ResultSet rows = statement.executeQuery()) {
            Map<String, Integer> counts = new LinkedHashMap<>();
            while (rows.next()) {
                counts.put(rows.getString("approval_status"), rows.getInt("total"));
            }
            return counts;
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("RedditItemDao.countByStatus failed", e);
        }
    }

    /** Moves a PENDING post to APPROVED or REJECTED. False when it was not pending (or does not exist). */
    public boolean setApproval(int itemId, String newStatus) {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(SET_APPROVAL_SQL)) {
            statement.setString(1, newStatus);
            statement.setInt(2, itemId);
            statement.setString(3, PENDING);
            return statement.executeUpdate() == 1;
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("RedditItemDao.setApproval failed", e);
        }
    }

    public Optional<String> findPostUrl(int itemId) {
        try (Connection connection = DatabaseConfig.getConnection()) {
            return findPostUrl(connection, itemId);
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("RedditItemDao.findPostUrl failed", e);
        }
    }

    /** Same as findPostUrl, on a connection the caller already has open (used by ItemDao for the item page). */
    static Optional<String> findPostUrl(Connection connection, int itemId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_POST_URL_SQL)) {
            statement.setInt(1, itemId);
            try (ResultSet row = statement.executeQuery()) {
                return row.next() ? Optional.of(row.getString("post_url")) : Optional.empty();
            }
        }
    }
}
