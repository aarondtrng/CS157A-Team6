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
import util.CsrfUtil;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GET /profile/edit?id= shows the edit form and POST saves it. Only the profile's owner may use it: guests
 * are sent to log in and other students get a 403. The POST needs the CSRF token and validates every field
 * again on the server. The profile image is not editable here yet (it keeps its current value).
 */
@WebServlet("/profile/edit")
public class EditProfileServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(EditProfileServlet.class.getName());
    private static final String VIEW = "/WEB-INF/views/edit-profile.jsp";

    private final UserProfileService profileService = new UserProfileService(new UserDao());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer userId = ownerId(request, response);
        if (userId == null) {
            return;
        }

        try {
            Optional<UserProfile> profile = profileService.getProfile(userId);
            if (profile.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            UserProfile current = profile.get();
            showForm(request, response, current, current.getName(), current.getPhoneNumber(),
                    current.getProfileDescription(), Map.of(), HttpServletResponse.SC_OK);

        } catch (SQLException | IllegalStateException e) {
            fail(response, "Profile could not be loaded for editing", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        Integer userId = ownerId(request, response);
        if (userId == null) {
            return;
        }

        try {
            Optional<UserProfile> existing = profileService.getProfile(userId);
            if (existing.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String name = profileService.normalizeName(request.getParameter("name"));
            String phoneNumber = profileService.normalizePhone(request.getParameter("phoneNumber"));
            String description = profileService.normalizeText(request.getParameter("profileDescription"));

            if (!CsrfUtil.isValid(request)) {
                // Missing or wrong token: a stale form (session expired) or one submitted from another site.
                showForm(request, response, existing.get(), name, phoneNumber, description,
                        Map.of("form", "Your session expired. Please check your changes and save again."),
                        HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            Map<String, String> errors = profileService.validate(name, phoneNumber, description);
            if (!errors.isEmpty()) {
                showForm(request, response, existing.get(), name, phoneNumber, description, errors,
                        HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            boolean updated = profileService.updateProfile(
                    userId,
                    name,
                    phoneNumber.isEmpty() ? null : phoneNumber,
                    description.isEmpty() ? null : description,
                    existing.get().getProfileImage());
            if (!updated) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            AuthUtil.refreshDisplayName(request, response, name);
            response.sendRedirect(request.getContextPath() + "/profile?id=" + userId);

        } catch (SQLException | IllegalStateException e) {
            fail(response, "Profile could not be saved", e);
        }
    }

    /**
     * The id of the profile being edited, but only when the logged-in user owns it. Otherwise sends the
     * response (404 for a bad id, login redirect for a guest, 403 for someone else's profile) and returns null.
     */
    private Integer ownerId(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int requestedId;
        try {
            requestedId = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        Integer currentUserId = AuthUtil.currentUserId(request);
        if (currentUserId == null) {
            String next = URLEncoder.encode(AuthUtil.currentPathWithQuery(request), StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + AuthUtil.LOGIN_PATH + "?next=" + next);
            return null;
        }
        if (currentUserId != requestedId) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return requestedId;
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response, UserProfile profile,
                          String name, String phoneNumber, String profileDescription,
                          Map<String, String> errors, int status) throws ServletException, IOException {
        request.setAttribute("profile", profile);
        request.setAttribute("name", name);
        request.setAttribute("phoneNumber", phoneNumber);
        request.setAttribute("profileDescription", profileDescription);
        request.setAttribute("errors", errors);
        request.setAttribute("csrfToken", CsrfUtil.token(request));
        response.setStatus(status);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private void fail(HttpServletResponse response, String message, Exception cause) throws IOException {
        // Log the cause server-side; users get a generic error, never database details.
        LOG.log(Level.SEVERE, message, cause);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}
