package servlet;

import dao.ItemDao;
import dto.CatalogQuery;
import dto.CatalogSort;
import service.CatalogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Public item catalog. Search, filter, sort, and paging are GET query parameters; the page is rendered by JSP. */
@WebServlet("/items")
public class CatalogServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/catalog.jsp";

    private final CatalogService catalogService = new CatalogService(new ItemDao());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CatalogQuery query = catalogService.buildQuery(
                request.getParameter("q"),
                request.getParameter("category"),
                request.getParameter("sort"),
                request.getParameter("page"));

        request.setAttribute("query", query);
        request.setAttribute("sortOptions", CatalogSort.values());

        try {
            request.setAttribute("categories", catalogService.getCategories());
            request.setAttribute("catalogPage", catalogService.getCatalogPage(query));
        } catch (UnsupportedOperationException e) {
            // DAO SQL has not been written yet: say so instead of showing an empty or fake catalog.
            response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
            request.setAttribute("notImplemented", true);
        }

        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
