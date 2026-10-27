package servlet;

import dao.DataAccessException;
import dao.ItemDao;
import dao.RequestDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import service.RequestService;
import util.AuthUtil;
import util.CsrfUtil;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * POST /requests/action: accept, reject, complete (the post's creator) or cancel (the requester) one
 * request, then redirects back to /requests. The service and DAO check that the request really is the
 * logged-in student's to change, so a guessed requestId does nothing.
 */
@WebServlet("/requests/action")
public class RequestActionServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(RequestActionServlet.class.getName());

    private final RequestService requestService = new RequestService(new ItemDao(), new RequestDao());

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

        Integer requestId = requestService.parseId(request.getParameter("requestId"));
        if (requestId == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            boolean changed = requestService.applyAction(userId, requestId, request.getParameter("action"));
            response.sendRedirect(request.getContextPath() + "/requests?" + (changed ? "updated=1" : "failed=1"));
        } catch (DataAccessException e) {
            LOG.log(Level.SEVERE, "Request could not be updated", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
