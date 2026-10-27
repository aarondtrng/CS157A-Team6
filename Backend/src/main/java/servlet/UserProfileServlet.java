package servlet;

import dao.UserDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.UserProfile;
import service.UserProfileService;
import util.AuthUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GET /profile?id= shows a student's profile. Anyone can see the name and description; the email and phone
 * number are shown only to logged-in students (the JSP checks loggedIn), and only the owner sees the edit link.
 */
@WebServlet("/profile")
public class UserProfileServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(UserProfileServlet.class.getName());
    private static final String VIEW = "/WEB-INF/views/profile.jsp";

    private final UserProfileService profileService = new UserProfileService(new UserDao());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int userId;
        try {
            userId = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            Optional<UserProfile> profile = profileService.getProfile(userId);
            if (profile.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            Integer currentUserId = AuthUtil.currentUserId(request);
            request.setAttribute("profile", profile.get());
            request.setAttribute("loggedIn", currentUserId != null);
            request.setAttribute("isOwner", currentUserId != null && currentUserId == userId);
            request.getRequestDispatcher(VIEW).forward(request, response);

        } catch (SQLException | IllegalStateException e) {
            // Log the cause server-side; users get a generic error, never database details.
            LOG.log(Level.SEVERE, "Profile could not be loaded", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
