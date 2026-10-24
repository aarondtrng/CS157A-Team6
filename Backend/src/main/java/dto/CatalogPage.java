package dto;

import model.ItemSummary;

import java.util.List;

/** One page of catalog results plus the paging details the JSP needs. */
public class CatalogPage {
    private final List<ItemSummary> items;
    private final int totalCount;
    private final int page;
    private final int pageSize;

    public CatalogPage(List<ItemSummary> items, int totalCount, int page, int pageSize) {
        this.items = items;
        this.totalCount = totalCount;
        this.page = page;
        this.pageSize = pageSize;
    }

    public List<ItemSummary> getItems() {
        return items;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getPage() {
        return page;
    }

    public int getTotalPages() {
        return Math.max(1, (totalCount + pageSize - 1) / pageSize);
    }

    public boolean isHasPrevious() {
        return page > 1;
    }

    public boolean isHasNext() {
        return page < getTotalPages();
    }
}
