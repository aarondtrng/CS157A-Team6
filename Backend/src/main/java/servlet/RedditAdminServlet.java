package servlet;

import dao.DataAccessException;
import dao.ItemDao;
import dao.RedditItemDao;
import dao.UserDao;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import service.RedditScrapeService;
import util.AuthUtil;
import util.CsrfUtil;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Buttons in the admin dashboard's "Pending Reddit posts" section (POST only). Every action redirects
 * back to the dashboard, so refreshing the page never runs an action twice.
 *   action=fetch                 import new r/SJSU posts as pending items
 *   action=approve&itemId=...    make a pending import public
 *   action=reject&itemId=...     keep it hidden for good (it is never imported again)
 */
@WebServlet("/admin/reddit/action")
public class RedditAdminServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(RedditAdminServlet.class.getName());
    private static final String DASHBOARD = "/admin/users";

    private final RedditScrapeService service =
            new RedditScrapeService(new RedditItemDao(), new ItemDao(), new UserDao());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!User.ROLE_ADMINISTRATOR.equals(AuthUtil.currentRole(request)) || !CsrfUtil.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String dashboard = request.getContextPath() + DASHBOARD;
        String action = request.getParameter("action");
        try {
            if ("fetch".equals(action)) {
                RedditScrapeService.ScrapeResult result = service.scrape();
                if (result.errorKey() != null) {
                    response.sendRedirect(dashboard + "?redditError=" + result.errorKey() + "#reddit");
                } else {
                    response.sendRedirect(dashboard + "?added=" + result.added() + "&dupes=" + result.duplicates()
                            + "&failed=" + result.failed() + "#reddit");
                }
                return;
            }

            if ("approve".equals(action) || "reject".equals(action)) {
                Integer itemId = parsePositive(request.getParameter("itemId"));
                if (itemId == null) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                service.decide(itemId, "approve".equals(action));
                response.sendRedirect(dashboard + "#reddit");
                return;
            }

            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (DataAccessException e) {
            LOG.log(Level.WARNING, "Reddit admin action failed: " + action, e);
            response.sendRedirect(dashboard + "?redditError=database#reddit");
        }
    }

    private static Integer parsePositive(String value) {
        if (value == null) {
            return null;
        }
        try {
            int id = Integer.parseInt(value.strip());
            return id > 0 ? id : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
