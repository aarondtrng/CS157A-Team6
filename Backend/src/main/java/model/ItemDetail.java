package model;

import java.time.LocalDateTime;
import java.util.List;

/** A full item post: the Item columns, category name, poster details, ordered images, and optional location. */
public class ItemDetail {
    private final int itemId;
    private final String title;
    private final String description;
    private final String type;
    private final String status;
    private final String categoryName;
    private final LocalDateTime createdAt;
    private final int posterUserId;
    private final String posterName;
    /** Poster's profile image, or null (the page then shows initials). */
    private final String posterProfileImage;
    /** Image URLs in display_order; empty when the post has no images. */
    private final List<String> imageUrls;
    /** At most one per item; null when no location was set. */
    private final ItemLocation location;

    public ItemDetail(int itemId, String title, String description, String type, String status,
                      String categoryName, LocalDateTime createdAt, int posterUserId, String posterName,
                      String posterProfileImage, List<String> imageUrls, ItemLocation location) {
        this.itemId = itemId;
        this.title = title;
        this.description = description;
        this.type = type;
        this.status = status;
        this.categoryName = categoryName;
        this.createdAt = createdAt;
        this.posterUserId = posterUserId;
        this.posterName = posterName;
        this.posterProfileImage = posterProfileImage;
        this.imageUrls = List.copyOf(imageUrls);
        this.location = location;
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

    public int getPosterUserId() {
        return posterUserId;
    }

    public String getPosterName() {
        return posterName;
    }

    public String getPosterProfileImage() {
        return posterProfileImage;
    }

    /** Default avatar text: first letters of the first two words of the poster's name. */
    public String getPosterInitials() {
        if (posterName == null || posterName.isBlank()) {
            return "?";
        }
        String[] words = posterName.strip().split("\\s+");
        String initials = words[0].substring(0, 1);
        if (words.length > 1) {
            initials += words[1].substring(0, 1);
        }
        return initials.toUpperCase();
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public ItemLocation getLocation() {
        return location;
    }
}
