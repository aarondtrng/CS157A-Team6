package service;

import dao.ItemDao;
import model.ItemDetail;

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
}
