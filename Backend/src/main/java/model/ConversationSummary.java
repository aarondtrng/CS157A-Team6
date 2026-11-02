package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** One row of the inbox: the other student in a conversation, the latest message, and how many are unread. */
public class ConversationSummary {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("h:mm a", Locale.US);
    private static final DateTimeFormatter WEEKDAY = DateTimeFormatter.ofPattern("EEE", Locale.US);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d", Locale.US);

    private final int conversationId;
    private final int otherUserId;
    private final String otherName;
    /** Latest message text, or null when the conversation has no messages (or the latest has no text). */
    private final String lastText;
    private final LocalDateTime lastSentAt;
    private final Integer lastSenderId;
    private final int unreadCount;

    public ConversationSummary(int conversationId, int otherUserId, String otherName, String lastText,
                               LocalDateTime lastSentAt, Integer lastSenderId, int unreadCount) {
        this.conversationId = conversationId;
        this.otherUserId = otherUserId;
        this.otherName = otherName;
        this.lastText = lastText;
        this.lastSentAt = lastSentAt;
        this.lastSenderId = lastSenderId;
        this.unreadCount = unreadCount;
    }

    public int getConversationId() {
        return conversationId;
    }

    public int getOtherUserId() {
        return otherUserId;
    }

    public String getOtherName() {
        return otherName;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public boolean isUnread() {
        return unreadCount > 0;
    }

    /** First letter of the first and last name, upper case, for the default avatar. */
    public String getInitials() {
        return initialsOf(otherName);
    }

    /** One of five avatar colors, chosen from the other student's id so it stays the same everywhere. */
    public int getAvatarColor() {
        return otherUserId % 5;
    }

    /** The preview line. Messages the viewer sent start with "You: ". */
    public String getPreview() {
        if (lastText == null) {
            return lastSentAt == null ? "No messages yet" : "";
        }
        String flat = lastText.replaceAll("\\s+", " ").strip();
        if (flat.isEmpty()) {
            flat = "Photo";
        }
        return lastSenderId != null && lastSenderId != otherUserId ? "You: " + flat : flat;
    }

    /** "10:05 AM" for today, "Yesterday", the weekday within the last week, otherwise "Oct 24". */
    public String getWhen() {
        if (lastSentAt == null) {
            return "";
        }
        LocalDate today = LocalDate.now();
        LocalDate day = lastSentAt.toLocalDate();
        if (day.equals(today)) {
            return lastSentAt.format(TIME);
        }
        if (day.equals(today.minusDays(1))) {
            return "Yesterday";
        }
        if (day.isAfter(today.minusDays(7)) && day.isBefore(today)) {
            return lastSentAt.format(WEEKDAY);
        }
        return lastSentAt.format(DATE);
    }

    static String initialsOf(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        String[] parts = name.strip().split("\\s+");
        String first = parts[0].substring(0, 1).toUpperCase(Locale.ROOT);
        return parts.length == 1 ? first : first + parts[parts.length - 1].substring(0, 1).toUpperCase(Locale.ROOT);
    }
}
