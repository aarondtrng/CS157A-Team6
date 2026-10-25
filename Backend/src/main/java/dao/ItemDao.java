package dao;

import dto.CatalogQuery;
import model.Category;
import model.ItemDetail;
import model.ItemSummary;

import java.util.List;
import java.util.Optional;

/**
 * JDBC access for the item catalog.
 *
 * SKELETON: every method throws UnsupportedOperationException until the SQL is written and wired in.
 * Use PreparedStatement with bound parameters for every value taken from CatalogQuery.
 */
public class ItemDao {

    /**
     * Returns one page of publicly visible items for the catalog.
     *
     * TODO (SQL needed): read from Item, joined to Category for the category name and to ItemImage for
     * the image with the lowest display_order (items with no image still appear, thumbnail null).
     * - Only public posts: exclude removed items and RedditItem rows that an administrator has not
     *   approved. (RedditItem has no approval column yet; this is a known schema gap.)
     * - Search text: match against title and description. Treat the text literally, so wildcard
     *   characters typed by the user do not widen the search. Skip when query.hasSearchText() is false.
     * - Category filter: only when query.getCategoryId() is not null.
     * - Sort by query.getSort(): newest / oldest by created_at, A-Z / Z-A by title. Use a fixed mapping
     *   from the enum to column and direction; never insert user text into the statement.
     * - Bounded results: return at most query.getPageSize() rows, starting at query.getOffset().
     * Expected result: the matching ItemSummary list for that page, empty list when nothing matches.
     */
    public List<ItemSummary> searchPublicItems(CatalogQuery query) {
        throw new UnsupportedOperationException("ItemDao.searchPublicItems: SQL not implemented yet");
    }

    /**
     * Returns one publicly visible item with everything the full post page needs.
     *
     * TODO (SQL needed): read from Item joined to Category (category name) and User (poster user_id,
     * name, profile_image), plus all ItemImage rows for the item ordered by display_order, plus the
     * item's Location row if one exists (Item has at most one). Never read the poster's email,
     * phone number, or password_hash for this page.
     * - Same visibility rule as the catalog: removed items and RedditItem rows that an administrator
     *   has not approved must behave as if they do not exist.
     * - Build the location with new ItemLocation(...) so coordinates are rounded before leaving the DAO.
     * Expected result: Optional.of(detail) when found and public; Optional.empty() when no such public
     * item exists (the caller answers 404).
     */
    public Optional<ItemDetail> findPublicItemById(int itemId) {
        throw new UnsupportedOperationException("ItemDao.findPublicItemById: SQL not implemented yet");
    }

    /**
     * Counts all publicly visible items matching the same search text and category filter as
     * searchPublicItems, ignoring sort and paging, so the page can show total pages.
     *
     * TODO (SQL needed): same tables and filters as searchPublicItems. Expected result: a count (0 when
     * nothing matches).
     */
    public int countPublicItems(CatalogQuery query) {
        throw new UnsupportedOperationException("ItemDao.countPublicItems: SQL not implemented yet");
    }

    /**
     * Returns all categories for the filter dropdown, ordered by name.
     *
     * TODO (SQL needed): read from Category. Expected result: every category, empty list only if the
     * table has no rows.
     */
    public List<Category> findAllCategories() {
        throw new UnsupportedOperationException("ItemDao.findAllCategories: SQL not implemented yet");
    }
}
