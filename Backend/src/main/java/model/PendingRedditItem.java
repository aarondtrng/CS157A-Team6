package model;

import java.time.LocalDateTime;

/** One imported Reddit post as shown in the admin dashboard's review queue. */
public class PendingRedditItem {
    private final int itemId;
    private final String title;
    private final String description;
    private final String type;
    private final String categoryName;
    private final LocalDateTime createdAt;
    private final String postUrl;
    private final String approvalStatus;
    /** First image, or null when the post has none. */
    private final String thumbnailUrl;

    public PendingRedditItem(int itemId, String title, String description, String type, String categoryName,
                             LocalDateTime createdAt, String postUrl, String approvalStatus, String thumbnailUrl) {
        this.itemId = itemId;
        this.title = title;
        this.description = description;
        this.type = type;
        this.categoryName = categoryName;
        this.createdAt = createdAt;
        this.postUrl = postUrl;
        this.approvalStatus = approvalStatus;
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

    /** The first 200 characters of the description, for the review card. */
    public String getSnippet() {
        if (description == null) {
            return "";
        }
        return description.length() <= 200 ? description : description.substring(0, 200) + "…";
    }

    public String getType() {
        return type;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getCreatedDateText() {
        return createdAt == null ? "" : createdAt.toLocalDate().toString();
    }

    public String getPostUrl() {
        return postUrl;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}
