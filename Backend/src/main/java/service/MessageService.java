package service;

import dao.MessageDao;
import dao.UserDao;
import model.ChatMessage;
import model.ConversationSummary;
import model.User;
import model.UserProfile;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Business rules for messaging: who may be messaged, what a message must contain, and how a thread is
 * prepared for the page (day separators, "Seen" on the viewer's latest read message). Every operation takes
 * the logged-in student's id, and the DAO re-checks that the student belongs to the conversation.
 */
public class MessageService {
    public static final int MAX_TEXT_LENGTH = 5000;

    /** Everything the messages page shows: the inbox, the open conversation (if any) and its messages. */
    public static class Page {
        public final List<ConversationSummary> conversations;
        public final ConversationSummary selected;
        public final List<ChatMessage> messages;

        Page(List<ConversationSummary> conversations, ConversationSummary selected, List<ChatMessage> messages) {
            this.conversations = conversations;
            this.selected = selected;
            this.messages = messages;
        }
    }

    /** Held while a conversation between two students is looked up and created, so two clicks make one. */
    private static final Object START_LOCK = new Object();

    private final MessageDao messageDao;
    private final UserDao userDao;

    public MessageService(MessageDao messageDao, UserDao userDao) {
        this.messageDao = messageDao;
        this.userDao = userDao;
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

    /**
     * Loads the page for a student. With no conversation id the newest conversation is opened. Returns empty
     * when a conversation id was given that is not one of this student's (the caller answers 404).
     * Opening a conversation marks the other student's messages as read.
     */
    public Optional<Page> loadPage(int userId, Integer conversationId) {
        Integer openId = conversationId;
        if (openId == null) {
            List<ConversationSummary> first = messageDao.findConversations(userId);
            openId = first.isEmpty() ? null : first.get(0).getConversationId();
            if (openId == null) {
                return Optional.of(new Page(first, null, List.of()));
            }
        }

        Optional<ConversationSummary> open = messageDao.findConversationFor(userId, openId);
        if (open.isEmpty()) {
            return Optional.empty();
        }
        messageDao.markRead(userId, openId);
        List<ChatMessage> thread = messageDao.findMessages(openId);
        prepare(thread, userId);
        // Loaded after marking read, so the open conversation no longer shows an unread count.
        return Optional.of(new Page(messageDao.findConversations(userId), open.get(), thread));
    }

    /** Marks each message as the viewer's or not, adds day separators, and flags the latest read own message. */
    private static void prepare(List<ChatMessage> thread, int userId) {
        LocalDate previousDay = null;
        ChatMessage lastMine = null;
        for (ChatMessage message : thread) {
            message.setMine(message.getSenderId() == userId);
            LocalDate day = message.getDay();
            if (day != null && !day.equals(previousDay)) {
                message.setDayLabel(ChatMessage.dayLabelFor(day));
                previousDay = day;
            }
            if (message.isMine()) {
                lastMine = message;
            }
        }
        if (lastMine != null && lastMine.isRead()) {
            lastMine.setSeen(true);
        }
    }

    /** Trims the text and unifies line breaks, so what is checked and stored is the same. */
    public String normalizeText(String raw) {
        return raw == null ? "" : raw.replace("\r\n", "\n").replace('\r', '\n').strip();
    }

    /** Null when the text can be sent; otherwise a short code ("empty", "long", "chars") for the page. */
    public String checkText(String text) {
        if (text.isEmpty()) {
            return "empty";
        }
        if (text.length() > MAX_TEXT_LENGTH) {
            return "long";
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isISOControl(c) && c != '\n') {
                return "chars";
            }
        }
        return null;
    }

    /** Saves the message. False when the student is not in that conversation. */
    public boolean send(int userId, int conversationId, String text) {
        return messageDao.insertMessage(userId, conversationId, text);
    }

    /**
     * Finds or creates the 1:1 conversation between the student and another student.
     *
     * @return the conversation id, or empty when the other student does not exist, is not a student, or is
     *         the same person
     */
    public Optional<Integer> startConversation(int userId, int otherUserId) throws SQLException {
        if (userId == otherUserId) {
            return Optional.empty();
        }
        Optional<UserProfile> other = userDao.findProfileById(otherUserId);
        if (other.isEmpty() || !User.ROLE_STUDENT.equals(other.get().getRole())) {
            return Optional.empty();
        }
        synchronized (START_LOCK) {
            Optional<Integer> existing = messageDao.findDirectConversation(userId, otherUserId);
            if (existing.isPresent()) {
                return existing;
            }
            return Optional.of(messageDao.createConversation(userId, otherUserId));
        }
    }
}
