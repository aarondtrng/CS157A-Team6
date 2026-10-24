package dto;

/** Validated catalog search request. Built by CatalogService from raw query-string values. */
public class CatalogQuery {
    private final String searchText;
    private final Integer categoryId;
    private final CatalogSort sort;
    private final int page;
    private final int pageSize;

    public CatalogQuery(String searchText, Integer categoryId, CatalogSort sort, int page, int pageSize) {
        this.searchText = searchText;
        this.categoryId = categoryId;
        this.sort = sort;
        this.page = page;
        this.pageSize = pageSize;
    }

    /** Trimmed, length-bounded search text; empty when no search was entered. */
    public String getSearchText() {
        return searchText;
    }

    public boolean hasSearchText() {
        return !searchText.isEmpty();
    }

    /** Selected category, or null for all categories. */
    public Integer getCategoryId() {
        return categoryId;
    }

    public CatalogSort getSort() {
        return sort;
    }

    /** 1-based page number. */
    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getOffset() {
        return (page - 1) * pageSize;
    }
}
