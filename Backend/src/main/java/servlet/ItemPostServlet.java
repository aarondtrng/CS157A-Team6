package servlet;

import dao.DataAccessException;
import dao.ItemDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.ItemDetail;
import service.ItemPostService;
import util.AuthUtil;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Full item post at /item?id=. Public and read-only: guests can view posts but not interact with them.
 */
@WebServlet("/item")
public class ItemPostServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(ItemPostServlet.class.getName());
    private static final String VIEW = "/WEB-INF/views/item.jsp";

    private final ItemPostService itemPostService = new ItemPostService(new ItemDao());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer itemId = itemPostService.parseItemId(request.getParameter("id"));
        if (itemId == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Guests get a view-only page; the JSP hides actions for them (the action endpoints must still check).
        request.setAttribute("loggedIn", AuthUtil.isLoggedIn(request));

        try {
            Optional<ItemDetail> post = itemPostService.getPublicPost(itemId);
            if (post.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("post", post.get());
        } catch (DataAccessException e) {
            // Log the cause server-side; users get a generic error, never database details.
            LOG.log(Level.SEVERE, "Item post could not be loaded", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        } catch (UnsupportedOperationException e) {
            // DAO SQL has not been written yet: say so instead of showing a fake post.
            response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
            request.setAttribute("notImplemented", true);
        }

        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
