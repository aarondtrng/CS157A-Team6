package servlet;

import dao.DataAccessException;
import dao.ItemDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Category;
import model.MapItem;
import service.CatalogService;
import service.MapService;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The landing page at the site root (and /home): hero, the newest real posts, how it works, and the map
 * teaser. Public. If the database cannot be read, the page still loads without the posts and categories.
 */
@WebServlet(urlPatterns = {"", "/home"})
public class HomeServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(HomeServlet.class.getName());
    private static final String VIEW = "/WEB-INF/views/home.jsp";
    private static final int RECENT_COUNT = 4;
    private static final int POPULAR_CATEGORIES = 5;
    private static final int HERO_PINS = 12;

    private final CatalogService catalogService = new CatalogService(new ItemDao());
    private final MapService mapService = new MapService(new ItemDao());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("recentItems", catalogService.getRecentItems(RECENT_COUNT));
            List<Category> categories = catalogService.getCategories();
            request.setAttribute("popularCategories",
                    categories.subList(0, Math.min(POPULAR_CATEGORIES, categories.size())));
            // Located posts, newest first: the newest one is the pin in the map section, and the first few
            // are the pins on the hero map.
            List<MapItem> located = mapService.getMapItems();
            if (!located.isEmpty()) {
                request.setAttribute("latestPin", located.get(0));
            }
            request.setAttribute("heroPins", located.subList(0, Math.min(HERO_PINS, located.size())));
        } catch (DataAccessException | UnsupportedOperationException e) {
            // The landing page is still useful without the live sections; say so there instead of failing.
            LOG.log(Level.SEVERE, "Landing page data could not be loaded", e);
            request.setAttribute("dataUnavailable", true);
        }
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
