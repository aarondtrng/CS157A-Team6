package model;

import java.time.LocalDateTime;

/** Private report details, supplied only to the administrator view. */
public class ReportRow {
    private final int reportId;
    private final int reporterId;
    private final String reporterName;
    private final Integer itemId;
    private final int reportedUserId;
    private final String reportedUserName;
    private final String reason;
    private final String status;
    private final LocalDateTime reportDate;

    public ReportRow(int reportId, int reporterId, String reporterName, Integer itemId,
                     int reportedUserId, String reportedUserName, String reason, String status,
                     LocalDateTime reportDate) {
        this.reportId = reportId;
        this.reporterId = reporterId;
        this.reporterName = reporterName;
        this.itemId = itemId;
        this.reportedUserId = reportedUserId;
        this.reportedUserName = reportedUserName;
        this.reason = reason;
        this.status = status;
        this.reportDate = reportDate;
    }

    public int getReportId() { return reportId; }
    public int getReporterId() { return reporterId; }
    public String getReporterName() { return reporterName; }
    public Integer getItemId() { return itemId; }
    public int getReportedUserId() { return reportedUserId; }
    public String getReportedUserName() { return reportedUserName; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public String getReportDateText() { return reportDate == null ? "" : reportDate.toString().replace('T', ' '); }
    public boolean isPending() { return "PENDING".equals(status); }
}
