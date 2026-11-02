package servlet;

import dao.DataAccessException;
import dao.MessageDao;
import dao.UserDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import model.User;
import service.MessageService;
import util.AuthUtil;
import util.CsrfUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * POST /messages/send: saves a message (text, photos, or both) in one of the student's conversations, then redirects back to
 * it (Post/Redirect/Get). The DAO only inserts when the student really is in that conversation.
 */
@WebServlet("/messages/send")
@MultipartConfig(
        maxFileSize = MessageService.MAX_PHOTO_BYTES,
        maxRequestSize = (long) MessageService.MAX_PHOTO_BYTES * (MessageService.MAX_PHOTOS + 1))
public class MessageSendServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(MessageSendServlet.class.getName());

    private final MessageService messageService = new MessageService(new MessageDao(), new UserDao());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Integer userId = AuthUtil.currentUserId(request);
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + AuthUtil.LOGIN_PATH);
            return;
        }
        if (!User.ROLE_STUDENT.equals(AuthUtil.currentRole(request))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Read the photos first: reading the form fields also parses the multipart body.
        List<byte[]> photos = new ArrayList<>();
        boolean tooBig = false;
        try {
            for (Part part : request.getParts()) {
                if ("images".equals(part.getName()) && part.getSize() > 0) {
                    if (photos.size() > MessageService.MAX_PHOTOS) {
                        break; // Too many: the service reports it. Do not read more.
                    }
                    try (InputStream in = part.getInputStream()) {
                        photos.add(in.readNBytes(MessageService.MAX_PHOTO_BYTES + 1));
                    }
                }
            }
        } catch (IllegalStateException | IOException | ServletException e) {
            tooBig = true; // A file or the whole request exceeded the size limits.
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
        String problem = tooBig ? "photo-size" : messageService.checkMessage(text, photos);
        String target = request.getContextPath() + "/messages?c=" + conversationId;
        if (problem != null) {
            response.sendRedirect(target + "&error=" + problem);
            return;
        }

        try {
            boolean sent = messageService.send(userId, conversationId, text, photos);
            response.sendRedirect(sent ? target : target + "&error=failed");
        } catch (DataAccessException | UncheckedIOException e) {
            LOG.log(Level.SEVERE, "Message could not be sent", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
