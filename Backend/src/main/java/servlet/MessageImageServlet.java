package servlet;

import dao.DataAccessException;
import dao.MessageDao;
import dao.UserDao;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import service.MessageService;
import util.AuthUtil;
import util.ImageSniffer;
import util.ImageStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serves a photo sent in a message at /messages/image/<name>. Unlike item photos these are private: only
 * the two students in the conversation get the file, everyone else gets a 404.
 */
@WebServlet("/messages/image/*")
public class MessageImageServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(MessageImageServlet.class.getName());

    private final MessageService messageService = new MessageService(new MessageDao(), new UserDao());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer userId = AuthUtil.currentUserId(request);
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + AuthUtil.LOGIN_PATH);
            return;
        }
        if (!User.ROLE_STUDENT.equals(AuthUtil.currentRole(request))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String pathInfo = request.getPathInfo();
        String name = pathInfo == null || pathInfo.length() < 2 ? null : pathInfo.substring(1);
        Path file = ImageStore.resolveMessagePhoto(name);
        if (file == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            if (!messageService.canViewPhoto(userId, name) || !Files.isRegularFile(file)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        } catch (DataAccessException e) {
            LOG.log(Level.SEVERE, "Message photo could not be checked", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        String extension = name.substring(name.lastIndexOf('.') + 1);
        response.setContentType(ImageSniffer.contentTypeFor(extension));
        response.setContentLengthLong(Files.size(file));
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "private, max-age=3600");
        Files.copy(file, response.getOutputStream());
    }
}
