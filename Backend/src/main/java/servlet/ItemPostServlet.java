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
import model.ItemSummary;
import model.User;
import service.ItemPostService;
import service.RequestService;
import util.AuthUtil;

import java.io.IOException;
import java.util.List;
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
    private static final int RELATED_COUNT = 5;

    private final ItemPostService itemPostService = new ItemPostService(new ItemDao());
    private final RequestService requestService = new RequestService(new ItemDao(), new RequestDao());

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
            addRequestState(request, post.get());
            addRelatedItems(request, post.get());
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

    /** Other posts in the same category, shown beside the post. If they cannot be loaded the post still shows. */
    private void addRelatedItems(HttpServletRequest request, ItemDetail post) {
        try {
            List<ItemSummary> related = itemPostService.getRelatedItems(post, RELATED_COUNT);
            if (related.isEmpty()) {
                // Nothing else in this category: show the newest posts from anywhere so the space is still useful.
                related = itemPostService.getNewestOtherItems(post, RELATED_COUNT);
                request.setAttribute("relatedFallback", true);
            }
            request.setAttribute("relatedItems", related);
        } catch (DataAccessException e) {
            LOG.log(Level.SEVERE, "Related items could not be loaded for the item page", e);
        }
    }

    /**
     * Tells the page which ownership-request action to offer: "isStudent" (only students can request),
     * "isOwner" (the creator manages requests instead), and "myRequestStatus" (the student's active request
     * for this item, if any). If the request lookup fails the post still loads, just without the action.
     */
    private void addRequestState(HttpServletRequest request, ItemDetail post) {
        Integer userId = AuthUtil.currentUserId(request);
        boolean isStudent = userId != null && User.ROLE_STUDENT.equals(AuthUtil.currentRole(request));
        request.setAttribute("isStudent", isStudent);
        request.setAttribute("isOwner", userId != null && userId == post.getPosterUserId());
        if (isStudent && userId != post.getPosterUserId()) {
            try {
                requestService.findActiveRequestStatus(userId, post.getItemId())
                        .ifPresent(status -> request.setAttribute("myRequestStatus", status));
            } catch (DataAccessException e) {
                LOG.log(Level.SEVERE, "Request status could not be loaded for the item page", e);
            }
        }
    }
}
