package dao;

import config.DatabaseConfig;
import dto.CatalogQuery;
import dto.NewPost;
import model.Category;
import model.ItemDetail;
import model.ItemLocation;
import model.ItemSummary;
import model.MapItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC access for the item catalog and item posts.
 *
 * SQL here uses only course-approved features (see CLAUDE.md): SELECT / FROM / WHERE, JOIN ... ON, LIKE,
 * correlated subqueries, NOT EXISTS, ORDER BY, and COUNT. Every value from a request is bound with a
 * PreparedStatement parameter; the only text added to a statement is fixed fragments chosen in this class.
 *
 * Not yet tested against a real MySQL database.
 */
public class ItemDao {
    /**
     * ASSUMED status values. The schema leaves allowed values undefined (CLAUDE.md "Known schema gaps"):
     * an Item whose status is REMOVED is hidden, and a RedditItem is shown only once its approval_status
     * is APPROVED. Change these two constants if the team picks different values.
     */
    static final String STATUS_REMOVED = "REMOVED";
    static final String APPROVAL_APPROVED = "APPROVED";

    /** Public-visibility test shared by every query: not removed, and not an unapproved Reddit import. */
    private static final String PUBLIC_ONLY =
            " WHERE i.status <> ?"
            + " AND NOT EXISTS (SELECT * FROM RedditItem r"
            + " WHERE r.item_id = i.item_id AND r.approval_status <> ?)";

    /** The map shows at most this many of the newest located items, so the page stays bounded. */
    static final int MAX_MAP_ITEMS = 500;

    private static final String SEARCH_FILTER = " AND (i.title LIKE ? OR i.description LIKE ?)";
    private static final String CATEGORY_FILTER = " AND i.category_id = ?";

    /** The first image (lowest display_order) of the item, or NULL when it has none. */
    private static final String THUMBNAIL =
            "(SELECT ii.image_url FROM ItemImage ii"
            + " WHERE ii.item_id = i.item_id"
            + " AND ii.display_order = (SELECT MIN(ii2.display_order) FROM ItemImage ii2"
            + " WHERE ii2.item_id = i.item_id)) AS thumbnail_url";

    /**
     * Returns one page of publicly visible items for the catalog, filtered by search text and category
     * and sorted as requested. Empty list when nothing matches.
     *
     * Paging: the statement is not given a row limit (LIMIT is not on the approved SQL list). Instead the
     * driver is told to stop after offset + pageSize rows and this method skips the first offset rows.
     */
    public List<ItemSummary> searchPublicItems(CatalogQuery query) {
        String sql = "SELECT i.item_id, i.title, i.description, i.type, i.status, c.category_name,"
                + " i.created_at, " + THUMBNAIL
                + " FROM Item i JOIN Category c ON c.category_id = i.category_id"
                + filterSql(query)
                + orderBySql(query);

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindFilters(statement, query);
            statement.setMaxRows(query.getOffset() + query.getPageSize());

            List<ItemSummary> items = new ArrayList<>();
            try (ResultSet rows = statement.executeQuery()) {
                int skipped = 0;
                while (rows.next()) {
                    if (skipped < query.getOffset()) {
                        skipped++;
                        continue;
                    }
                    items.add(new ItemSummary(
                            rows.getInt("item_id"),
                            rows.getString("title"),
                            rows.getString("description"),
                            rows.getString("type"),
                            rows.getString("status"),
                            rows.getString("category_name"),
                            rows.getObject("created_at", LocalDateTime.class),
                            rows.getString("thumbnail_url")));
                }
            }
            return items;
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("ItemDao.searchPublicItems failed", e);
        }
    }

    /**
     * Returns one publicly visible item with its category, poster (id, name, profile image only: never
     * email, phone, or password hash), ordered images, and location if any. Optional.empty() when no such
     * public item exists (the caller answers 404).
     */
    public Optional<ItemDetail> findPublicItemById(int itemId) {
        String itemSql = "SELECT i.item_id, i.title, i.description, i.type, i.status, c.category_name,"
                + " i.category_id, i.created_at, u.user_id, u.name, u.profile_image"
                + " FROM Item i"
                + " JOIN Category c ON c.category_id = i.category_id"
                + " JOIN User u ON u.user_id = i.user_id"
                + PUBLIC_ONLY
                + " AND i.item_id = ?";
        String imagesSql = "SELECT image_url FROM ItemImage WHERE item_id = ? ORDER BY display_order";
        String locationSql = "SELECT location_name, latitude, longitude FROM Location WHERE item_id = ?";

        try (Connection connection = DatabaseConfig.getConnection()) {
            int itemIdValue;
            String title;
            String description;
            String type;
            String status;
            String categoryName;
            int categoryId;
            LocalDateTime createdAt;
            int posterUserId;
            String posterName;
            String posterProfileImage;

            try (PreparedStatement statement = connection.prepareStatement(itemSql)) {
                statement.setString(1, STATUS_REMOVED);
                statement.setString(2, APPROVAL_APPROVED);
                statement.setInt(3, itemId);
                try (ResultSet row = statement.executeQuery()) {
                    if (!row.next()) {
                        return Optional.empty();
                    }
                    itemIdValue = row.getInt("item_id");
                    title = row.getString("title");
                    description = row.getString("description");
                    type = row.getString("type");
                    status = row.getString("status");
                    categoryName = row.getString("category_name");
                    categoryId = row.getInt("category_id");
                    createdAt = row.getObject("created_at", LocalDateTime.class);
                    posterUserId = row.getInt("user_id");
                    posterName = row.getString("name");
                    posterProfileImage = row.getString("profile_image");
                }
            }

            List<String> imageUrls = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(imagesSql)) {
                statement.setInt(1, itemId);
                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        imageUrls.add(rows.getString("image_url"));
                    }
                }
            }

            ItemLocation location = null;
            try (PreparedStatement statement = connection.prepareStatement(locationSql)) {
                statement.setInt(1, itemId);
                try (ResultSet row = statement.executeQuery()) {
                    if (row.next()) {
                        // The coordinates are stored and shown exactly as the poster marked them.
                        location = new ItemLocation(
                                row.getString("location_name"),
                                row.getDouble("latitude"),
                                row.getDouble("longitude"));
                    }
                }
            }

            return Optional.of(new ItemDetail(itemIdValue, title, description, type, status, categoryName,
                    categoryId, createdAt, posterUserId, posterName, posterProfileImage, imageUrls, location));
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("ItemDao.findPublicItemById failed", e);
        }
    }

    /**
     * Counts all publicly visible items matching the same search text and category filter as
     * searchPublicItems (ignoring sort and paging), so the page can show total pages. 0 when none match.
     */
    public int countPublicItems(CatalogQuery query) {
        String sql = "SELECT COUNT(*) FROM Item i" + filterSql(query);

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindFilters(statement, query);
            try (ResultSet row = statement.executeQuery()) {
                return row.next() ? row.getInt(1) : 0;
            }
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("ItemDao.countPublicItems failed", e);
        }
    }

    /** Returns every category, ordered by name, for the filter dropdown. */
    public List<Category> findAllCategories() {
        String sql = "SELECT category_id, category_name FROM Category ORDER BY category_name";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            List<Category> categories = new ArrayList<>();
            while (rows.next()) {
                categories.add(new Category(rows.getInt("category_id"), rows.getString("category_name")));
            }
            return categories;
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("ItemDao.findAllCategories failed", e);
        }
    }

    /**
     * Saves a new post: one Item row, one Location row, and one ItemImage row per photo, all in a single
     * transaction, so a failure part-way leaves nothing behind. The status and created_at columns use
     * their table defaults (ACTIVE and the current time).
     *
     * @return the new item_id
     * @throws DataAccessException if any insert fails (everything is rolled back)
     */
    public int createItem(NewPost post) {
        String itemSql = "INSERT INTO Item (user_id, category_id, title, description, type)"
                + " VALUES (?, ?, ?, ?, ?)";
        String locationSql = "INSERT INTO Location (item_id, longitude, latitude, location_name)"
                + " VALUES (?, ?, ?, ?)";
        String imageSql = "INSERT INTO ItemImage (item_id, image_url, display_order) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseConfig.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int itemId;
                try (PreparedStatement statement =
                             connection.prepareStatement(itemSql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, post.userId());
                    statement.setInt(2, post.categoryId());
                    statement.setString(3, post.title());
                    statement.setString(4, post.description());
                    statement.setString(5, post.type());
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Insert into Item returned no generated item_id");
                        }
                        itemId = keys.getInt(1);
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(locationSql)) {
                    statement.setInt(1, itemId);
                    statement.setDouble(2, post.longitude());
                    statement.setDouble(3, post.latitude());
                    if (post.locationName() == null) {
                        statement.setNull(4, java.sql.Types.VARCHAR);
                    } else {
                        statement.setString(4, post.locationName());
                    }
                    statement.executeUpdate();
                }

                try (PreparedStatement statement = connection.prepareStatement(imageSql)) {
                    int displayOrder = 1;
                    for (String imageUrl : post.imageUrls()) {
                        statement.setInt(1, itemId);
                        statement.setString(2, imageUrl);
                        statement.setInt(3, displayOrder++);
                        statement.executeUpdate();
                    }
                }

                connection.commit();
                return itemId;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("ItemDao.createItem failed", e);
        }
    }

    /**
     * Returns the newest publicly visible items that have a location, for the campus map (at most
     * MAX_MAP_ITEMS). Items without a location are left out. Coordinates are returned exactly as
     * stored. The row cap is set on the statement because LIMIT is not on the
     * approved SQL list.
     */
    public List<MapItem> findMapItems() {
        String sql = "SELECT i.item_id, i.title, i.type, c.category_name,"
                + " l.location_name, l.latitude, l.longitude, " + THUMBNAIL
                + " FROM Item i"
                + " JOIN Category c ON c.category_id = i.category_id"
                + " JOIN Location l ON l.item_id = i.item_id"
                + PUBLIC_ONLY
                + " ORDER BY i.created_at DESC, i.item_id DESC";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, STATUS_REMOVED);
            statement.setString(2, APPROVAL_APPROVED);
            statement.setMaxRows(MAX_MAP_ITEMS);

            List<MapItem> items = new ArrayList<>();
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    items.add(new MapItem(
                            rows.getInt("item_id"),
                            rows.getString("title"),
                            rows.getString("type"),
                            rows.getString("category_name"),
                            new ItemLocation(
                                    rows.getString("location_name"),
                                    rows.getDouble("latitude"),
                                    rows.getDouble("longitude")),
                            rows.getString("thumbnail_url")));
                }
            }
            return items;
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("ItemDao.findMapItems failed", e);
        }
    }

    /** WHERE clause for the catalog: the public-only test plus the search and category filters in use. */
    private static String filterSql(CatalogQuery query) {
        StringBuilder sql = new StringBuilder(PUBLIC_ONLY);
        if (query.hasSearchText()) {
            sql.append(SEARCH_FILTER);
        }
        if (query.getCategoryId() != null) {
            sql.append(CATEGORY_FILTER);
        }
        return sql.toString();
    }

    /**
     * Binds the parameters of filterSql(query), in the same order. The ids and the search text are only
     * ever bound as values, never added to the statement text.
     */
    private static void bindFilters(PreparedStatement statement, CatalogQuery query) throws SQLException {
        int index = 1;
        statement.setString(index++, STATUS_REMOVED);
        statement.setString(index++, APPROVAL_APPROVED);
        if (query.hasSearchText()) {
            String pattern = "%" + escapeLike(query.getSearchText()) + "%";
            statement.setString(index++, pattern);
            statement.setString(index++, pattern);
        }
        if (query.getCategoryId() != null) {
            statement.setInt(index, query.getCategoryId());
        }
    }

    /**
     * Fixed ORDER BY text for each sort option (never built from user text). The item_id tiebreaker keeps
     * the order stable, so an item cannot repeat or vanish between pages.
     */
    private static String orderBySql(CatalogQuery query) {
        switch (query.getSort()) {
            case OLDEST:
                return " ORDER BY i.created_at ASC, i.item_id ASC";
            case TITLE_ASC:
                return " ORDER BY i.title ASC, i.item_id ASC";
            case TITLE_DESC:
                return " ORDER BY i.title DESC, i.item_id DESC";
            case NEWEST:
            default:
                return " ORDER BY i.created_at DESC, i.item_id DESC";
        }
    }

    /**
     * Makes user search text match literally: LIKE treats % and _ as wildcards, and MySQL's default escape
     * character is the backslash, so each of these three characters gets a backslash in front.
     */
    private static String escapeLike(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
