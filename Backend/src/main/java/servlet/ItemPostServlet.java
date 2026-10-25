package servlet;

import dao.ItemDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.ItemDetail;
import service.ItemPostService;

import java.io.IOException;
import java.util.Optional;

/**
 * Full item post at /item?id=. Guests are redirected to login by LoginRequiredFilter before this runs.
 */
@WebServlet("/item")
public class ItemPostServlet extends HttpServlet {
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

        try {
            Optional<ItemDetail> post = itemPostService.getPublicPost(itemId);
            if (post.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("post", post.get());
        } catch (UnsupportedOperationException e) {
            // DAO SQL has not been written yet: say so instead of showing a fake post.
            response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
            request.setAttribute("notImplemented", true);
        }

        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
