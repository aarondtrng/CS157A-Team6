package servlet;

import config.DatabaseConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.LocalRequests;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DEVELOPMENT ONLY: GET /dev/db-check reports whether the app can open a MySQL connection.
 *
 * It runs no SQL: it opens a connection through {@link DatabaseConfig} and uses the JDBC driver's own
 * isValid() check and metadata. Requests that do not come from the local machine get a 404, and the
 * page shows only a generic category of failure; the real exception goes to the server log.
 * Remove this servlet and dev/db-check.jsp before any real deployment.
 */
@WebServlet("/dev/db-check")
public class DbCheckServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(DbCheckServlet.class.getName());
    private static final String VIEW = "/WEB-INF/views/dev/db-check.jsp";
    private static final int VALID_TIMEOUT_SECONDS = 5;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!LocalRequests.isLocal(request)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        boolean connected = false;
        String message;
        try (Connection connection = DatabaseConfig.getConnection()) {
            if (connection.isValid(VALID_TIMEOUT_SECONDS)) {
                connected = true;
                DatabaseMetaData meta = connection.getMetaData();
                request.setAttribute("productName", meta.getDatabaseProductName());
                request.setAttribute("productVersion", meta.getDatabaseProductVersion());
                request.setAttribute("driverVersion", meta.getDriverVersion());
                message = "Connected and the connection is valid.";
            } else {
                message = "Connected, but the database did not answer the validity check in "
                        + VALID_TIMEOUT_SECONDS + " seconds.";
            }
        } catch (IllegalStateException e) {
            // DatabaseConfig names the missing setting (never its value), so this message is safe to show.
            message = e.getMessage();
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Database connection check failed", e);
            message = "Could not connect. Check that MySQL is running and that the URL, user, and password in "
                    + "Tomcat's bin/setenv.sh are correct. Details are in the Tomcat log.";
        }

        request.setAttribute("connected", connected);
        request.setAttribute("message", message);
        response.setStatus(connected ? HttpServletResponse.SC_OK : HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
