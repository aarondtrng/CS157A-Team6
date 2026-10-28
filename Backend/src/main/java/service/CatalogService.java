package service;

import dao.ItemDao;
import dto.CatalogPage;
import dto.CatalogQuery;
import dto.CatalogSort;
import model.Category;
import model.ItemSummary;

import java.util.List;

/** Validates catalog requests and loads catalog data through the DAO. */
public class CatalogService {
    static final int PAGE_SIZE = 12;
    static final int MAX_SEARCH_LENGTH = 100;
    static final int MAX_PAGE = 10_000;

    private final ItemDao itemDao;

    public CatalogService(ItemDao itemDao) {
        this.itemDao = itemDao;
    }

    /** Turns raw query-string values into a safe query; invalid or missing values fall back to defaults. */
    public CatalogQuery buildQuery(String search, String category, String sort, String page) {
        String searchText = search == null ? "" : search.strip();
        if (searchText.length() > MAX_SEARCH_LENGTH) {
            searchText = searchText.substring(0, MAX_SEARCH_LENGTH);
        }
        Integer categoryId = parsePositiveInt(category);
        int pageNumber = Math.min(MAX_PAGE, parsePositiveInt(page) == null ? 1 : parsePositiveInt(page));
        return new CatalogQuery(searchText, categoryId, CatalogSort.fromParam(sort), pageNumber, PAGE_SIZE);
    }

    public CatalogPage getCatalogPage(CatalogQuery query) {
        int total = itemDao.countPublicItems(query);
        return new CatalogPage(itemDao.searchPublicItems(query), total, query.getPage(), query.getPageSize());
    }

    /** The newest publicly visible items (at most count), for the landing page. Uses the catalog query. */
    public List<ItemSummary> getRecentItems(int count) {
        return itemDao.searchPublicItems(new CatalogQuery("", null, CatalogSort.NEWEST, 1, count));
    }

    public List<Category> getCategories() {
        return itemDao.findAllCategories();
    }

    private static Integer parsePositiveInt(String value) {
        if (value == null) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(value.strip());
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
