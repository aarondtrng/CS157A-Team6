package servlet;

import dao.UserDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.UserProfile;
import service.UserProfileService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/profile")
public class UserProfileServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/profile.jsp";

    private final UserProfileService profileService =
            new UserProfileService(new UserDao());

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String idParameter = request.getParameter("id");
        int userId;

        try {
            userId = Integer.parseInt(idParameter);
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            Optional<UserProfile> profile =
                    profileService.getProfile(userId);

            if (profile.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            request.setAttribute("profile", profile.get());
            request.getRequestDispatcher(VIEW).forward(request, response);

        } catch (SQLException e) {
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}