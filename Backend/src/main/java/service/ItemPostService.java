package service;

import dao.ItemDao;
import dto.CatalogQuery;
import dto.CatalogSort;
import model.ItemDetail;
import model.ItemSummary;

import java.util.List;
import java.util.Optional;

/** Loads full item posts. */
public class ItemPostService {
    private final ItemDao itemDao;

    public ItemPostService(ItemDao itemDao) {
        this.itemDao = itemDao;
    }

    /** Parses the id request parameter; null when missing, not a number, or not positive. */
    public Integer parseItemId(String value) {
        if (value == null) {
            return null;
        }
        try {
            int id = Integer.parseInt(value.strip());
            return id > 0 ? id : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** @throws UnsupportedOperationException until the ItemDao SQL is implemented */
    public Optional<ItemDetail> getPublicPost(int itemId) {
        return itemDao.findPublicItemById(itemId);
    }

    /**
     * Up to limit other publicly visible posts in the same category as this one, newest first. Uses the
     * catalog query (one extra row is asked for, so the post itself can be left out).
     */
    public List<ItemSummary> getRelatedItems(ItemDetail post, int limit) {
        CatalogQuery query = new CatalogQuery("", post.getCategoryId(), CatalogSort.NEWEST, 1, limit + 1);
        return itemDao.searchPublicItems(query).stream()
                .filter(item -> item.getItemId() != post.getItemId())
                .limit(limit)
                .toList();
    }

    /** Up to limit of the newest other publicly visible posts, from any category. Used when a post has no category mates. */
    public List<ItemSummary> getNewestOtherItems(ItemDetail post, int limit) {
        CatalogQuery query = new CatalogQuery("", null, CatalogSort.NEWEST, 1, limit + 1);
        return itemDao.searchPublicItems(query).stream()
                .filter(item -> item.getItemId() != post.getItemId())
                .limit(limit)
                .toList();
    }

    /** A user's own publicly visible posts, newest first, for their profile page. */
    public List<ItemSummary> getPostsByUser(int userId, int limit) {
        return itemDao.findPublicItemsByUser(userId, limit);
    }
}
