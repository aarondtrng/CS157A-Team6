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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GET /requests: the logged-in student's ownership requests, in two lists: the requests they made, and the
 * requests other students made on their posts (with the private evidence, which only these two students
 * can see). Students only. Status changes are POSTed to /requests/action.
 */
@WebServlet("/requests")
public class RequestsServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(RequestsServlet.class.getName());
    private static final String VIEW = "/WEB-INF/views/requests.jsp";

    private final RequestService requestService = new RequestService(new ItemDao(), new RequestDao());

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

        try {
            request.setAttribute("made", requestService.getRequestsMadeBy(userId));
            request.setAttribute("received", requestService.getRequestsOnPostsOf(userId));
        } catch (DataAccessException e) {
            LOG.log(Level.SEVERE, "Requests could not be loaded", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        request.setAttribute("csrfToken", CsrfUtil.token(request));
        // One-time messages after a redirect (Post/Redirect/Get); only these exact values are shown.
        request.setAttribute("sent", "1".equals(request.getParameter("sent")));
        request.setAttribute("updated", "1".equals(request.getParameter("updated")));
        request.setAttribute("failed", "1".equals(request.getParameter("failed")));
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
