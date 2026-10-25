package servlet;

import dao.DataAccessException;
import dao.ItemDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.MapService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Public campus map at /map: a marker for each item that has a location. Guests can view it. */
@WebServlet("/map")
public class MapServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(MapServlet.class.getName());
    private static final String VIEW = "/WEB-INF/views/map.jsp";

    private final MapService mapService = new MapService(new ItemDao());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("mapItems", mapService.getMapItems());
        } catch (DataAccessException e) {
            // Log the cause server-side; users get a generic error, never database details.
            LOG.log(Level.SEVERE, "Map items could not be loaded", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
