package model;

/**
 * Status values of an ownership request (ItemRequest.status). ASSUMED values: the schema leaves them
 * undefined (CLAUDE.md "Known schema gaps"). Keep them separate from the item's own status.
 *
 * PENDING is the start state. The post's creator moves it to ACCEPTED or REJECTED, and an ACCEPTED request
 * to COMPLETED (the item was handed back) or REJECTED. The requester can CANCEL a PENDING or ACCEPTED
 * request. PENDING and ACCEPTED are the "active" states: a student has at most one active request per item.
 */
public final class RequestStatus {
    public static final String PENDING = "PENDING";
    public static final String ACCEPTED = "ACCEPTED";
    public static final String REJECTED = "REJECTED";
    public static final String COMPLETED = "COMPLETED";
    public static final String CANCELLED = "CANCELLED";

    private RequestStatus() {
    }

    public static boolean isActive(String status) {
        return PENDING.equals(status) || ACCEPTED.equals(status);
    }
}
