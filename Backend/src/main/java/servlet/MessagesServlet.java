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
import java.util.List;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GET /messages?c=: the logged-in student's inbox and, beside it, one conversation (the one in "c", else the
 * newest). Students only; only a conversation's two participants can open it. Sending goes through
 * POST /messages/send and starting a conversation through POST /messages/start. New messages show when the
 * page is reloaded.
 */
@WebServlet("/messages")
public class MessagesServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(MessagesServlet.class.getName());
    private static final String VIEW = "/WEB-INF/views/messages.jsp";

    private final MessageService messageService = new MessageService(new MessageDao(), new UserDao());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer userId = AuthUtil.currentUserId(request);
        if (userId == null) {
            String next = URLEncoder.encode(AuthUtil.currentPathWithQuery(request), StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + AuthUtil.LOGIN_PATH + "?next=" + next);
            return;
        }
        if (!User.ROLE_STUDENT.equals(AuthUtil.currentRole(request))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String rawId = request.getParameter("c");
        Integer conversationId = messageService.parseId(rawId);
        if (rawId != null && !rawId.isBlank() && conversationId == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            Optional<MessageService.Page> page = messageService.loadPage(userId, conversationId);
            if (page.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("conversations", page.get().conversations);
            request.setAttribute("selected", page.get().selected);
            request.setAttribute("thread", page.get().messages);
        } catch (DataAccessException e) {
            LOG.log(Level.SEVERE, "Messages could not be loaded", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        request.setAttribute("csrfToken", CsrfUtil.token(request));
        // One-time message after a redirect; only these exact values are shown.
        String error = request.getParameter("error");
        if (error != null
                && List.of("empty", "long", "chars", "failed", "photo-count", "photo-size", "photo-type").contains(error)) {
            request.setAttribute("sendError", error);
        }
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
