package servlet;

import dao.UserDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.UserProfile;
import service.UserProfileService;
import util.AuthUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/profile/edit")
public class EditProfileServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/edit-profile.jsp";

    private final UserProfileService profileService =
            new UserProfileService(new UserDao());

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        Integer userId = getRequestedUserId(request);

        if (userId == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (!isCurrentUser(request, userId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
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

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        Integer userId = getRequestedUserId(request);

        if (userId == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (!isCurrentUser(request, userId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String name = request.getParameter("name");
        String phoneNumber = request.getParameter("phoneNumber");
        String profileDescription =
                request.getParameter("profileDescription");
        String profileImage = request.getParameter("profileImage");

        try {
            profileService.updateProfile(
                    userId,
                    name,
                    phoneNumber,
                    profileDescription,
                    profileImage);

            response.sendRedirect(
                    request.getContextPath() + "/profile?id=" + userId);

        } catch (SQLException e) {
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private Integer getRequestedUserId(HttpServletRequest request) {
        try {
            return Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    private boolean isCurrentUser(
            HttpServletRequest request,
            int requestedUserId) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return false;
        }

        Object sessionUserId =
                session.getAttribute(AuthUtil.SESSION_USER_ID);

        return sessionUserId instanceof Integer
                && (Integer) sessionUserId == requestedUserId;
    }
}