package servlet;

import dao.DataAccessException;
import dao.ItemDao;
import dao.RequestDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.ItemDetail;
import model.User;
import service.RequestService;
import util.AuthUtil;
import util.CsrfUtil;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * "Request this item": GET /request/new?itemId= shows the form, POST /request/new saves the request and
 * redirects to the requests page. Students only (checked here as well as by LoginRequiredFilter).
 */
@WebServlet("/request/new")
public class RequestFormServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(RequestFormServlet.class.getName());
    private static final String VIEW = "/WEB-INF/views/request-form.jsp";

    private final RequestService requestService = new RequestService(new ItemDao(), new RequestDao());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!allowed(request, response)) {
            return;
        }
        Integer itemId = requestService.parseId(request.getParameter("itemId"));
        if (itemId == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        render(request, response, itemId, "", Map.of(), HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!allowed(request, response)) {
            return;
        }
        Integer itemId = requestService.parseId(request.getParameter("itemId"));
        if (itemId == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String description = request.getParameter("description");
        String shown = description == null ? "" : description;

        if (!CsrfUtil.isValid(request)) {
            // Missing or wrong token: a stale form (session expired) or one submitted from another site.
            render(request, response, itemId, shown,
                    Map.of("form", "Your session expired. Please check your request and send it again."),
                    HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            Map<String, String> errors = requestService.submit(AuthUtil.currentUserId(request), itemId, description);
            if (errors.isEmpty()) {
                // Post/Redirect/Get: refreshing the next page cannot send the request twice.
                response.sendRedirect(request.getContextPath() + "/requests?sent=1");
                return;
            }
            render(request, response, itemId, shown, errors, HttpServletResponse.SC_BAD_REQUEST);
        } catch (DataAccessException e) {
            LOG.log(Level.SEVERE, "Request could not be saved", e);
            render(request, response, itemId, shown,
                    Map.of("form", "We could not send your request. Please try again in a moment."),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /** True for a logged-in student; otherwise sends guests to log in and everyone else a 403. */
    private boolean allowed(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer userId = AuthUtil.currentUserId(request);
        if (userId == null) {
            String next = URLEncoder.encode(AuthUtil.currentPathWithQuery(request), StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + AuthUtil.LOGIN_PATH + "?next=" + next);
            return false;
        }
        if (!User.ROLE_STUDENT.equals(AuthUtil.currentRole(request))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    private void render(HttpServletRequest request, HttpServletResponse response, int itemId,
                        String description, Map<String, String> errors, int status)
            throws ServletException, IOException {
        try {
            Optional<ItemDetail> item = requestService.findItem(itemId);
            if (item.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("post", item.get());
            // When the student cannot request this item, the page shows the reason instead of the form.
            request.setAttribute("blockedReason",
                    requestService.blockedReason(AuthUtil.currentUserId(request), item.get()));
        } catch (DataAccessException e) {
            LOG.log(Level.SEVERE, "Item could not be loaded for the request form", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        request.setAttribute("description", description);
        request.setAttribute("errors", new LinkedHashMap<>(errors));
        request.setAttribute("csrfToken", CsrfUtil.token(request));
        request.setAttribute("minLength", RequestService.MIN_DESCRIPTION_LENGTH);
        request.setAttribute("maxLength", RequestService.MAX_DESCRIPTION_LENGTH);
        response.setStatus(status);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
