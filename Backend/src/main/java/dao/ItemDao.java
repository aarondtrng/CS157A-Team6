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

/** Database access for catalog items and posts. */
public class ItemDao {
    /** Status values we picked ourselves. the schema doesn't define them yet. */
    static final String STATUS_REMOVED = "REMOVED";
    static final String APPROVAL_APPROVED = "APPROVED";

    /** Filter for valid items */
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

    /** Gets one page of public items for the catalog, using the search, category and sort the user chose. */
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

    /** Loads a full public post (images, location, basic poster info), or empty if it doesn't exist. */
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

    /** Counts the matching public items so we know how many pages there are. */
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

    /** Saves the item, its location and its photos together; if any part fails, none of it is saved. */
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

    /** Gets the newest public items that have a location, for the campus map. */
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

    /** Gets one user's public posts, newest first, for their profile page. */
    public List<ItemSummary> findPublicItemsByUser(int userId, int maxItems) {
        String sql = "SELECT i.item_id, i.title, i.description, i.type, i.status, c.category_name,"
                + " i.created_at, " + THUMBNAIL
                + " FROM Item i JOIN Category c ON c.category_id = i.category_id"
                + PUBLIC_ONLY
                + " AND i.user_id = ?"
                + " ORDER BY i.created_at DESC, i.item_id DESC";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, STATUS_REMOVED);
            statement.setString(2, APPROVAL_APPROVED);
            statement.setInt(3, userId);
            statement.setMaxRows(maxItems);

            List<ItemSummary> items = new ArrayList<>();
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
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
            throw new DataAccessException("ItemDao.findPublicItemsByUser failed", e);
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

    /** Fills in the ? values for the filters, in the same order filterSql added them. */
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

    /** Picks the ORDER BY for the chosen sort; item_id breaks ties so paging stays consistent. */
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

    /** Escapes %, _ and \ so the search text is matched literally. */
    private static String escapeLike(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
