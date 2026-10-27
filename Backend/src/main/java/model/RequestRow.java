package model;

import java.time.LocalDateTime;

/**
 * One ownership request as shown on the requests page. The same shape serves both lists: for a request the
 * student made, the other person is the post's creator; for a request on the student's own post, the other
 * person is the requester. Only the other person's id and name are carried, never email or phone.
 */
public class RequestRow {
    private final int requestId;
    private final int itemId;
    private final String itemTitle;
    private final int otherUserId;
    private final String otherUserName;
    /** The requester's private identifying evidence. Shown only to the requester and the post's creator. */
    private final String description;
    private final String status;
    private final LocalDateTime createdAt;

    public RequestRow(int requestId, int itemId, String itemTitle, int otherUserId, String otherUserName,
                      String description, String status, LocalDateTime createdAt) {
        this.requestId = requestId;
        this.itemId = itemId;
        this.itemTitle = itemTitle;
        this.otherUserId = otherUserId;
        this.otherUserName = otherUserName;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getRequestId() {
        return requestId;
    }

    public int getItemId() {
        return itemId;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public int getOtherUserId() {
        return otherUserId;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedDateText() {
        return createdAt == null ? "" : createdAt.toLocalDate().toString();
    }

    public boolean isPending() {
        return RequestStatus.PENDING.equals(status);
    }

    public boolean isAccepted() {
        return RequestStatus.ACCEPTED.equals(status);
    }

    public boolean isActive() {
        return RequestStatus.isActive(status);
    }
}
