package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Category;
import model.ItemDetail;
import service.AdminService;
import util.AuthUtil;
import util.CsrfUtil;
import model.User;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet("/admin/items/edit")
public class AdminItemServlet extends HttpServlet {
    private final AdminService adminService =
            new AdminService(new dao.ItemDao());

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if (!User.ROLE_ADMINISTRATOR.equals(AuthUtil.currentRole(request))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            int itemId = Integer.parseInt(request.getParameter("itemId"));

            Optional<ItemDetail> item =
                    adminService.getItem(itemId);

            if (item.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            List<Category> categories = adminService.getCategories();

            request.setAttribute("item", item.get());
            request.setAttribute("categories", categories);
            request.setAttribute("csrfToken", CsrfUtil.token(request));

            request.getRequestDispatcher(
                    "/WEB-INF/views/admin-item-edit.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
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
            int itemId = Integer.parseInt(request.getParameter("itemId"));
            int userId = Integer.parseInt(request.getParameter("userId"));
            int categoryId = Integer.parseInt(request.getParameter("categoryId"));

            String title = request.getParameter("title");
            String description = request.getParameter("description");
            String type = request.getParameter("type");

            adminService.updateItem(
                    itemId,
                    title,
                    description,
                    type,
                    categoryId);

            response.sendRedirect(
                    request.getContextPath()
                            + "/admin/users?id=" + userId);

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
