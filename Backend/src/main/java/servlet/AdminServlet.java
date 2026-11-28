package servlet;

import dao.UserDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import model.UserProfile;
import service.AdminService;
import service.RedditScrapeService;
import service.UserProfileService;
import util.AuthUtil;
import util.CsrfUtil;

import dao.DataAccessException;
import dao.RedditItemDao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/admin/users")
public class AdminServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(AdminServlet.class.getName());

    private final UserProfileService profileService =
            new UserProfileService(new UserDao());

    private final AdminService adminService =
            new AdminService(new dao.ItemDao());

    private final RedditScrapeService redditService =
            new RedditScrapeService(new RedditItemDao(), new dao.ItemDao(), new UserDao());

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if (!User.ROLE_ADMINISTRATOR.equals(AuthUtil.currentRole(request))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String name = request.getParameter("name");
        String idText = request.getParameter("id");
        List<UserProfile> profiles;
        Integer selectedUserId = null;

        try {
            if (idText != null && !idText.isBlank()) {
                int userId = Integer.parseInt(idText.strip());

                if (userId <= 0) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                selectedUserId = userId;

                Optional<UserProfile> profile =
                        profileService.getProfile(userId);

                profiles = profile.isPresent()
                        ? List.of(profile.get())
                        : List.of();
            } else {
                profiles = profileService.searchProfilesByName(name);
            }

            request.setAttribute("profiles", profiles);
            request.setAttribute("searchName", name == null ? "" : name);
            request.setAttribute("searchId", idText == null ? "" : idText);
            request.setAttribute("selectedUserId", selectedUserId);

            if (selectedUserId != null) {
                request.setAttribute(
                        "items",
                        adminService.getItemsForUser(selectedUserId));
            } else {
                request.setAttribute("items", List.of());
            }
            
            request.setAttribute("csrfToken", CsrfUtil.token(request));
            setRedditQueue(request);

            request.getRequestDispatcher(
                    "/WEB-INF/views/admin-users.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * The "Pending Reddit posts" section: the review queue, the counts per approval status, and the result
     * of the last Fetch (passed back in the redirect from RedditAdminServlet).
     */
    private void setRedditQueue(HttpServletRequest request) {
        try {
            request.setAttribute("redditPending", redditService.getPending());
            request.setAttribute("redditCounts", redditService.getCounts());
        } catch (DataAccessException e) {
            LOG.log(Level.WARNING, "Could not load the Reddit review queue", e);
            request.setAttribute("redditPending", List.of());
            request.setAttribute("redditCounts", java.util.Map.of());
            request.setAttribute("redditError",
                    RedditScrapeService.ERROR_MESSAGES.get("database"));
        }

        String errorKey = request.getParameter("redditError");
        if (errorKey != null && RedditScrapeService.ERROR_MESSAGES.containsKey(errorKey)) {
            request.setAttribute("redditError", RedditScrapeService.ERROR_MESSAGES.get(errorKey));
        }
        Integer added = parseCount(request.getParameter("added"));
        if (added != null) {
            request.setAttribute("redditAdded", added);
            request.setAttribute("redditDupes", parseCount(request.getParameter("dupes")));
            request.setAttribute("redditFailed", parseCount(request.getParameter("failed")));
        }
    }

    private static Integer parseCount(String value) {
        if (value == null || !value.matches("\\d{1,4}")) {
            return null;
        }
        return Integer.parseInt(value);
    }

@Override
protected void doPost(
        HttpServletRequest request,
        HttpServletResponse response)
        throws IOException {

    if (!User.ROLE_ADMINISTRATOR.equals(AuthUtil.currentRole(request))) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
    }

    if (!CsrfUtil.isValid(request)) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
    }

    try {
        String action = request.getParameter("action");
        int userId = Integer.parseInt(request.getParameter("userId"));

        if ("deleteProfile".equals(action)) {
            adminService.deleteProfile(userId);

            response.sendRedirect(
                    request.getContextPath() + "/admin/users");
            return;
        }

        int itemId = Integer.parseInt(request.getParameter("itemId"));
        adminService.removeItem(itemId);

        response.sendRedirect(
                request.getContextPath() + "/admin/users?id=" + userId);

    } catch (NumberFormatException e) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);

    } catch (SQLException e) {
        response.sendError(HttpServletResponse.SC_CONFLICT);
    }
    }
}