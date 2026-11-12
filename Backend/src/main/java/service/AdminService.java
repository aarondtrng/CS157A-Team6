package service;

import dao.ItemDao;
import dao.UserDao;
import model.ItemSummary;
import model.Category;
import model.ItemDetail;

import java.util.Optional;
import java.util.List;
import java.sql.SQLException;

public class AdminService {
    private static final int MAX_ITEMS = 100;

    private final ItemDao itemDao;

    private final UserDao userDao = new UserDao();

    public AdminService(ItemDao itemDao) {
        this.itemDao = itemDao;
    }

    public List<ItemSummary> getItemsForUser(int userId) {
        return itemDao.findPublicItemsByUser(userId, MAX_ITEMS);
    }

    public boolean removeItem(int itemId) {
        return itemDao.removeItem(itemId);
    }

    public boolean updateItem(
        int itemId,
        String title,
        String description,
        String type,
        int categoryId) {

        return itemDao.updateItem(
            itemId,
            title,
            description,
            type,
            categoryId);
    }
    
    public boolean deleteProfile(int userId) throws SQLException {
        return userDao.deleteProfile(userId);
    }

    public Optional<ItemDetail> getItem(int itemId) {
        return itemDao.findPublicItemById(itemId);
    }

    public List<Category> getCategories() {
        return itemDao.findAllCategories();
    }
}