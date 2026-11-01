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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * POST /messages/send: saves a text message in one of the student's conversations, then redirects back to
 * it (Post/Redirect/Get). The DAO only inserts when the student really is in that conversation.
 */
@WebServlet("/messages/send")
public class MessageSendServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(MessageSendServlet.class.getName());

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

        Integer conversationId = messageService.parseId(request.getParameter("conversationId"));
        if (conversationId == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String text = messageService.normalizeText(request.getParameter("text"));
        String problem = messageService.checkText(text);
        String target = request.getContextPath() + "/messages?c=" + conversationId;
        if (problem != null) {
            response.sendRedirect(target + "&error=" + problem);
            return;
        }

        try {
            boolean sent = messageService.send(userId, conversationId, text);
            response.sendRedirect(sent ? target : target + "&error=failed");
        } catch (DataAccessException e) {
            LOG.log(Level.SEVERE, "Message could not be sent", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
