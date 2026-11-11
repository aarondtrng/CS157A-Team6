package model;

/** A profile or public item; the reported user is always loaded on the server. */
public class ReportTarget {
    private final Integer itemId;
    private final int userId;
    private final String label;

    public ReportTarget(Integer itemId, int userId, String label) {
        this.itemId = itemId;
        this.userId = userId;
        this.label = label;
    }

    public Integer getItemId() { return itemId; }
    public int getUserId() { return userId; }
    public String getLabel() { return label; }
    public String getPath() { return itemId == null ? "/profile?id=" + userId : "/item?id=" + itemId; }
}
