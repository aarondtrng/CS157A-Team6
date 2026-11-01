package servlet;

import dao.DataAccessException;
import dao.MessageDao;
import dao.UserDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import service.MessageService;
import util.AuthUtil;
import util.CsrfUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * POST /messages/start: the Message button on a profile or post. Opens the existing conversation with that
 * student or creates one, then redirects to it. Students only, and only with other students.
 */
@WebServlet("/messages/start")
public class MessageStartServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(MessageStartServlet.class.getName());

    private final MessageService messageService = new MessageService(new MessageDao(), new UserDao());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer userId = AuthUtil.currentUserId(request);
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + AuthUtil.LOGIN_PATH);
            return;
        }
        if (!User.ROLE_STUDENT.equals(AuthUtil.currentRole(request))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (!CsrfUtil.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Integer otherUserId = messageService.parseId(request.getParameter("userId"));
        if (otherUserId == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            Optional<Integer> conversationId = messageService.startConversation(userId, otherUserId);
            if (conversationId.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.sendRedirect(request.getContextPath() + "/messages?c=" + conversationId.get());
        } catch (SQLException | DataAccessException e) {
            LOG.log(Level.SEVERE, "Conversation could not be started", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
