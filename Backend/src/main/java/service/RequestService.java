package service;

import dao.ItemDao;
import dao.RequestDao;
import model.ItemDetail;
import model.RequestRow;
import model.RequestStatus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Business rules for ownership requests: who may request an item, what a request must contain, and which
 * status changes the post's creator and the requester may make. The DAO enforces the same rules again in
 * its SQL, so a forged form cannot get around them.
 */
public class RequestService {
    public static final int MIN_DESCRIPTION_LENGTH = 10;
    public static final int MAX_DESCRIPTION_LENGTH = 5000;

    public static final String ACTION_ACCEPT = "accept";
    public static final String ACTION_REJECT = "reject";
    public static final String ACTION_COMPLETE = "complete";
    public static final String ACTION_CANCEL = "cancel";

    private final ItemDao itemDao;
    private final RequestDao requestDao;

    public RequestService(ItemDao itemDao, RequestDao requestDao) {
        this.itemDao = itemDao;
        this.requestDao = requestDao;
    }

    /** Parses an id request parameter; null when missing, not a number, or not positive. */
    public Integer parseId(String value) {
        if (value == null) {
            return null;
        }
        try {
            int id = Integer.parseInt(value.strip());
            return id > 0 ? id : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** The publicly visible post a student wants to request, or empty when there is none. */
    public Optional<ItemDetail> findItem(int itemId) {
        return itemDao.findPublicItemById(itemId);
    }

    /**
     * Why this student cannot request this post right now, or null when they can. The messages are shown
     * on the request page.
     */
    public String blockedReason(int userId, ItemDetail item) {
        if (!"FOUND".equals(item.getType())) {
            return "Ownership requests are only for items that were found.";
        }
        if (!RequestDao.ITEM_ACTIVE.equals(item.getStatus())) {
            return "This item is no longer available to request.";
        }
        if (item.getPosterUserId() == userId) {
            return "You cannot request your own post.";
        }
        if (requestDao.findActiveRequestStatus(userId, item.getItemId()).isPresent()) {
            return "You already have an active request for this item. You can follow it on your requests page.";
        }
        return null;
    }

    /** The status of the student's active request for the item, or empty. For the item page. */
    public Optional<String> findActiveRequestStatus(int userId, int itemId) {
        return requestDao.findActiveRequestStatus(userId, itemId);
    }

    /** Trims the text and unifies line breaks, so what is checked and stored is the same. */
    public String normalizeDescription(String raw) {
        return raw == null ? "" : raw.replace("\r\n", "\n").replace('\r', '\n').strip();
    }

    /**
     * Checks the request and saves it as PENDING.
     *
     * @return the errors to show, keyed by field ("description" or "form"); empty when the request was saved
     */
    public Map<String, String> submit(int userId, int itemId, String rawDescription) {
        Map<String, String> errors = new LinkedHashMap<>();

        Optional<ItemDetail> item = findItem(itemId);
        if (item.isEmpty()) {
            errors.put("form", "That item could not be found.");
            return errors;
        }
        String blocked = blockedReason(userId, item.get());
        if (blocked != null) {
            errors.put("form", blocked);
            return errors;
        }

        String description = normalizeDescription(rawDescription);
        String descriptionError = checkDescription(description);
        if (descriptionError != null) {
            errors.put("description", descriptionError);
            return errors;
        }

        if (!requestDao.createRequest(userId, itemId, description)) {
            // The rules are checked again inside the INSERT; this is the case where something changed
            // between the check above and the save (a second tab, or the post was just returned).
            errors.put("form", "Your request could not be sent. You may already have an active request, "
                    + "or the item is no longer available.");
        }
        return errors;
    }

    /** Null when the text is acceptable; otherwise the message to show. */
    static String checkDescription(String description) {
        if (description.length() < MIN_DESCRIPTION_LENGTH) {
            return "Describe how you can prove this item is yours (at least " + MIN_DESCRIPTION_LENGTH
                    + " characters).";
        }
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            return "Please keep this under " + MAX_DESCRIPTION_LENGTH + " characters.";
        }
        for (int i = 0; i < description.length(); i++) {
            char c = description.charAt(i);
            if (Character.isISOControl(c) && c != '\n') {
                return "The text contains characters that are not allowed.";
            }
        }
        return null;
    }

    public List<RequestRow> getRequestsMadeBy(int userId) {
        return requestDao.findRequestsMadeBy(userId);
    }

    public List<RequestRow> getRequestsOnPostsOf(int userId) {
        return requestDao.findRequestsOnPostsOf(userId);
    }

    /**
     * Applies one status change for the logged-in student. accept and reject are for the creator of the
     * requested post, complete is the creator finishing the return (request COMPLETED and item RETURNED
     * together), and cancel is for the student who made the request.
     *
     * @return true when the change was made; false for an unknown action, a request that is not this
     *         student's to change, or a status that does not allow the change
     */
    public boolean applyAction(int userId, int requestId, String action) {
        if (action == null) {
            return false;
        }
        switch (action) {
            case ACTION_ACCEPT:
                return requestDao.changeStatusAsPoster(requestId, userId, RequestStatus.ACCEPTED,
                        RequestStatus.PENDING);
            case ACTION_REJECT:
                return requestDao.changeStatusAsPoster(requestId, userId, RequestStatus.REJECTED,
                        RequestStatus.PENDING, RequestStatus.ACCEPTED);
            case ACTION_COMPLETE:
                return requestDao.completeRequest(requestId, userId);
            case ACTION_CANCEL:
                return requestDao.cancelRequest(requestId, userId);
            default:
                return false;
        }
    }
}
