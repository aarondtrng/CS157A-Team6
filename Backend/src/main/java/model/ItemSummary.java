package model;

import java.time.LocalDateTime;

/** One catalog entry: the Item columns plus its category name and first image. */
public class ItemSummary {
    private static final int BRIEF_LENGTH = 140;

    private final int itemId;
    private final String title;
    private final String description;
    private final String type;
    private final String status;
    private final String categoryName;
    private final LocalDateTime createdAt;
    /** Image with the lowest display_order, or null when the item has no images. */
    private final String thumbnailUrl;

    public ItemSummary(int itemId, String title, String description, String type, String status,
                       String categoryName, LocalDateTime createdAt, String thumbnailUrl) {
        this.itemId = itemId;
        this.title = title;
        this.description = description;
        this.type = type;
        this.status = status;
        this.categoryName = categoryName;
        this.createdAt = createdAt;
        this.thumbnailUrl = thumbnailUrl;
    }

    public int getItemId() {
        return itemId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    /** Short description for catalog cards. */
    public String getBriefDescription() {
        if (description == null || description.length() <= BRIEF_LENGTH) {
            return description;
        }
        return description.substring(0, BRIEF_LENGTH).stripTrailing() + "…";
    }

    /** LOST or FOUND. */
    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getCreatedDateText() {
        return createdAt == null ? "" : createdAt.toLocalDate().toString();
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}
