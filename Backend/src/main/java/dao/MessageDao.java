package dao;

import config.DatabaseConfig;
import util.ImageStore;
import model.ChatMessage;
import model.ConversationSummary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Database access for conversations and messages. */
public class MessageDao {
    /** Caps so the inbox and a thread stay a reasonable size. */
    static final int MAX_CONVERSATIONS = 100;
    static final int MAX_MESSAGES = 200;

    /** Id of the newest message in a conversation. */
    private static final String LATEST_ID =
            "(SELECT MAX(m2.message_id) FROM Message m2 WHERE m2.conversation_id = cp.conversation_id)";

    /** The user's conversations, newest first, with the last message and unread count. */
    public List<ConversationSummary> findConversations(int userId) {
        String sql = "SELECT cp.conversation_id, u.user_id, u.name,"
                + " (SELECT m.text FROM Message m WHERE m.message_id = " + LATEST_ID + ") AS last_text,"
                + " (SELECT m.sent_at FROM Message m WHERE m.message_id = " + LATEST_ID + ") AS last_sent_at,"
                + " (SELECT m.sender_id FROM Message m WHERE m.message_id = " + LATEST_ID + ") AS last_sender_id,"
                + " (SELECT COUNT(*) FROM Message m3 WHERE m3.conversation_id = cp.conversation_id"
                + " AND m3.sender_id <> cp.user_id AND m3.read_status = 0) AS unread_count"
                + " FROM ConversationParticipant cp"
                + " JOIN ConversationParticipant other ON other.conversation_id = cp.conversation_id"
                + " AND other.user_id <> cp.user_id"
                + " JOIN User u ON u.user_id = other.user_id"
                + " WHERE cp.user_id = ?"
                + " ORDER BY " + LATEST_ID + " DESC, cp.conversation_id DESC";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setMaxRows(MAX_CONVERSATIONS);

            List<ConversationSummary> conversations = new ArrayList<>();
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    int senderId = rows.getInt("last_sender_id");
                    Integer lastSender = rows.wasNull() ? null : senderId;
                    conversations.add(new ConversationSummary(
                            rows.getInt("conversation_id"),
                            rows.getInt("user_id"),
                            rows.getString("name"),
                            rows.getString("last_text"),
                            rows.getObject("last_sent_at", LocalDateTime.class),
                            lastSender,
                            rows.getInt("unread_count")));
                }
            }
            return conversations;
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("MessageDao.findConversations failed", e);
        }
    }

    /** Gets the other person in a conversation. Empty if the user isn't in it. */
    public Optional<ConversationSummary> findConversationFor(int userId, int conversationId) {
        String sql = "SELECT me.conversation_id, u.user_id, u.name"
                + " FROM ConversationParticipant me"
                + " JOIN ConversationParticipant other ON other.conversation_id = me.conversation_id"
                + " AND other.user_id <> me.user_id"
                + " JOIN User u ON u.user_id = other.user_id"
                + " WHERE me.user_id = ? AND me.conversation_id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, conversationId);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    return Optional.empty();
                }
                return Optional.of(new ConversationSummary(rows.getInt("conversation_id"),
                        rows.getInt("user_id"), rows.getString("name"), null, null, null, 0));
            }
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("MessageDao.findConversationFor failed", e);
        }
    }

    /** The latest messages in a conversation, oldest first. */
    public List<ChatMessage> findMessages(int conversationId) {
        String sql = "SELECT message_id, sender_id, text, sent_at, read_status FROM Message"
                + " WHERE conversation_id = ? ORDER BY message_id DESC";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, conversationId);
            statement.setMaxRows(MAX_MESSAGES);

            List<ChatMessage> messages = new ArrayList<>();
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    messages.add(new ChatMessage(
                            rows.getInt("message_id"),
                            rows.getInt("sender_id"),
                            rows.getString("text"),
                            rows.getObject("sent_at", LocalDateTime.class),
                            rows.getInt("read_status") != 0));
                }
            }
            Collections.reverse(messages);
            if (!messages.isEmpty()) {
                addPhotos(connection, conversationId, messages);
            }
            return messages;
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("MessageDao.findMessages failed", e);
        }
    }

    /** Attaches each message's photos (file names only) to the loaded messages. */
    private static void addPhotos(Connection connection, int conversationId, List<ChatMessage> messages)
            throws SQLException {
        String sql = "SELECT a.message_id, a.image_url FROM MessageAttachment a"
                + " JOIN Message m ON m.message_id = a.message_id"
                + " WHERE m.conversation_id = ? AND a.message_id >= ?"
                + " ORDER BY a.attachment_id";

        Map<Integer, ChatMessage> byId = new HashMap<>();
        for (ChatMessage message : messages) {
            byId.put(message.getMessageId(), message);
        }
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, conversationId);
            statement.setInt(2, messages.get(0).getMessageId());
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    ChatMessage message = byId.get(rows.getInt("message_id"));
                    String url = rows.getString("image_url");
                    if (message != null && url != null && url.startsWith(ImageStore.MESSAGE_PREFIX)) {
                        message.addImage(url.substring(ImageStore.MESSAGE_PREFIX.length()));
                    }
                }
            }
        }
    }

    /** True if the user is in the conversation that the photo was sent in. */
    public boolean canViewPhoto(int userId, String imageUrl) {
        String sql = "SELECT a.attachment_id FROM MessageAttachment a"
                + " JOIN Message m ON m.message_id = a.message_id"
                + " JOIN ConversationParticipant cp ON cp.conversation_id = m.conversation_id"
                + " WHERE a.image_url = ? AND cp.user_id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, imageUrl);
            statement.setInt(2, userId);
            statement.setMaxRows(1);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("MessageDao.canViewPhoto failed", e);
        }
    }

    /** Marks the other person's messages as read. */
    public void markRead(int userId, int conversationId) {
        String sql = "UPDATE Message SET read_status = 1"
                + " WHERE conversation_id = ? AND sender_id <> ? AND read_status = 0"
                + " AND EXISTS (SELECT * FROM ConversationParticipant cp"
                + " WHERE cp.conversation_id = ? AND cp.user_id = ?)";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, conversationId);
            statement.setInt(2, userId);
            statement.setInt(3, conversationId);
            statement.setInt(4, userId);
            statement.executeUpdate();
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("MessageDao.markRead failed", e);
        }
    }

    /** Saves a message and its photos together. Returns false if the sender isn't in the conversation. */
    public boolean insertMessage(int senderId, int conversationId, String text, List<String> imageUrls) {
        String messageSql = "INSERT INTO Message (sender_id, conversation_id, text)"
                + " SELECT ?, ?, ? FROM ConversationParticipant"
                + " WHERE user_id = ? AND conversation_id = ?";
        String photoSql = "INSERT INTO MessageAttachment (message_id, image_url) VALUES (?, ?)";

        try (Connection connection = DatabaseConfig.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int messageId;
                try (PreparedStatement statement =
                             connection.prepareStatement(messageSql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, senderId);
                    statement.setInt(2, conversationId);
                    statement.setString(3, text);
                    statement.setInt(4, senderId);
                    statement.setInt(5, conversationId);
                    if (statement.executeUpdate() != 1) {
                        connection.rollback();
                        return false;
                    }
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("No message id was returned");
                        }
                        messageId = keys.getInt(1);
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(photoSql)) {
                    for (String imageUrl : imageUrls) {
                        statement.setInt(1, messageId);
                        statement.setString(2, imageUrl);
                        statement.executeUpdate();
                    }
                }
                connection.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("MessageDao.insertMessage failed", e);
        }
    }

    /** Finds the conversation between two users, if there is one. */
    public Optional<Integer> findDirectConversation(int userA, int userB) {
        String sql = "SELECT p1.conversation_id FROM ConversationParticipant p1"
                + " JOIN ConversationParticipant p2 ON p2.conversation_id = p1.conversation_id"
                + " WHERE p1.user_id = ? AND p2.user_id = ?"
                + " ORDER BY p1.conversation_id";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userA);
            statement.setInt(2, userB);
            statement.setMaxRows(1);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(rows.getInt("conversation_id")) : Optional.empty();
            }
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("MessageDao.findDirectConversation failed", e);
        }
    }

    /** Creates a conversation with both users in it and returns its id. */
    public int createConversation(int userA, int userB) {
        String conversationSql = "INSERT INTO Conversation (created_at) VALUES (CURRENT_TIMESTAMP)";
        String participantSql = "INSERT INTO ConversationParticipant (user_id, conversation_id) VALUES (?, ?)";

        try (Connection connection = DatabaseConfig.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int conversationId;
                try (PreparedStatement statement =
                             connection.prepareStatement(conversationSql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("No conversation id was returned");
                        }
                        conversationId = keys.getInt(1);
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(participantSql)) {
                    for (int userId : new int[]{userA, userB}) {
                        statement.setInt(1, userId);
                        statement.setInt(2, conversationId);
                        statement.executeUpdate();
                    }
                }
                connection.commit();
                return conversationId;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException | IllegalStateException e) {
            throw new DataAccessException("MessageDao.createConversation failed", e);
        }
    }
}
