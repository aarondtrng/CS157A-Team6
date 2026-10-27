package model;

/**
 * One marker on the campus map: an item that has a location, with just enough to show a popup and link
 * to the full post. The location is exactly as the poster marked it (see ItemLocation).
 */
public class MapItem {
    private final int itemId;
    private final String title;
    private final String type;
    private final String categoryName;
    private final ItemLocation location;
    /** The post's first photo URL, or null when it has none (the popup then shows no photo). */
    private final String thumbnailUrl;

    public MapItem(int itemId, String title, String type, String categoryName, ItemLocation location,
                   String thumbnailUrl) {
        this.itemId = itemId;
        this.title = title;
        this.type = type;
        this.categoryName = categoryName;
        this.location = location;
        this.thumbnailUrl = thumbnailUrl;
    }

    public int getItemId() {
        return itemId;
    }

    public String getTitle() {
        return title;
    }

    /** LOST or FOUND. */
    public String getType() {
        return type;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public ItemLocation getLocation() {
        return location;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}
