package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** One message in a conversation, as the page shows it. */
public class ChatMessage {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("h:mm a", Locale.US);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d", Locale.US);

    private final int messageId;
    private final int senderId;
    private final String text;
    private final LocalDateTime sentAt;
    private final boolean read;
    /** File names of the photos attached to this message, in the order they were sent. */
    private final List<String> images = new ArrayList<>();
    /** Set by the service: true for the viewer's own messages. */
    private boolean mine;
    /** Set by the service on the first message of each day ("TODAY", "YESTERDAY", "OCT 24"); else null. */
    private String dayLabel;
    /** Set by the service on the viewer's latest message when the other student has read it. */
    private boolean seen;

    public ChatMessage(int messageId, int senderId, String text, LocalDateTime sentAt, boolean read) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.text = text;
        this.sentAt = sentAt;
        this.read = read;
    }

    public int getMessageId() {
        return messageId;
    }

    public int getSenderId() {
        return senderId;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public boolean isRead() {
        return read;
    }

    public List<String> getImages() {
        return images;
    }

    public void addImage(String fileName) {
        images.add(fileName);
    }

    public boolean isMine() {
        return mine;
    }

    public void setMine(boolean mine) {
        this.mine = mine;
    }

    public String getDayLabel() {
        return dayLabel;
    }

    public void setDayLabel(String dayLabel) {
        this.dayLabel = dayLabel;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getTimeText() {
        return sentAt == null ? "" : sentAt.format(TIME);
    }

    public LocalDate getDay() {
        return sentAt == null ? null : sentAt.toLocalDate();
    }

    /** Label for a day separator: TODAY, YESTERDAY, or the date. */
    static String labelFor(LocalDate day) {
        LocalDate today = LocalDate.now();
        if (day.equals(today)) {
            return "TODAY";
        }
        if (day.equals(today.minusDays(1))) {
            return "YESTERDAY";
        }
        return day.format(DATE).toUpperCase(Locale.ROOT);
    }

    public static String dayLabelFor(LocalDate day) {
        return labelFor(day);
    }
}
