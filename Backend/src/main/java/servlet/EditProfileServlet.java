package servlet;

import dao.UserDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import model.UserProfile;
import service.UserProfileService;
import util.AuthUtil;
import util.CsrfUtil;
import util.ImageSniffer;
import util.ImageStore;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GET /profile/edit?id= shows the edit form and POST saves it. Only the profile's owner may use it: guests
 * are sent to log in and other students get a 403. The POST needs the CSRF token and validates every field
 * again on the server. The form is multipart because it can carry a new profile picture (JPEG or PNG, checked
 * from its bytes). A new picture replaces the old one, "Remove picture" clears it, and neither keeps it.
 */
@WebServlet("/profile/edit")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = UserProfileService.MAX_IMAGE_BYTES,
        maxRequestSize = UserProfileService.MAX_IMAGE_BYTES + 64 * 1024)
public class EditProfileServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(EditProfileServlet.class.getName());
    private static final String VIEW = "/WEB-INF/views/edit-profile.jsp";
    private static final String IMAGE_FIELD = "profileImage";

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

            UserProfile current = existing.get();

            // Read the picture first: reading the form fields also parses the multipart body.
            byte[] image = null;
            try {
                Part part = request.getPart(IMAGE_FIELD);
                if (part != null && part.getSize() > 0) {
                    try (InputStream in = part.getInputStream()) {
                        image = in.readNBytes(UserProfileService.MAX_IMAGE_BYTES + 1);
                    }
                }
            } catch (IllegalStateException | IOException | ServletException e) {
                // The picture or the whole request exceeded the size limits; the other fields are lost too.
                showForm(request, response, current, current.getName(), current.getPhoneNumber(),
                        current.getProfileDescription(),
                        Map.of(IMAGE_FIELD, "Profile picture must be 5 MB or smaller."),
                        HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            String name = profileService.normalizeName(request.getParameter("name"));
            String phoneNumber = profileService.normalizePhone(request.getParameter("phoneNumber"));
            String description = profileService.normalizeText(request.getParameter("profileDescription"));
            boolean removeImage = request.getParameter("removeImage") != null;

            if (!CsrfUtil.isValid(request)) {
                // Missing or wrong token: a stale form (session expired) or one submitted from another site.
                showForm(request, response, current, name, phoneNumber, description,
                        Map.of("form", "Your session expired. Please check your changes and save again."),
                        HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            Map<String, String> errors = new LinkedHashMap<>(profileService.validate(name, phoneNumber, description));
            if (image != null) {
                String imageError = profileService.checkImage(image);
                if (imageError != null) {
                    errors.put(IMAGE_FIELD, imageError);
                }
            }
            if (!errors.isEmpty()) {
                showForm(request, response, current, name, phoneNumber, description, errors,
                        HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            String oldImage = current.getProfileImage();
            String newImage = oldImage;
            String savedImage = null;
            if (image != null) {
                savedImage = ImageStore.save(image, ImageSniffer.extensionFor(image));
                newImage = savedImage;
            } else if (removeImage) {
                newImage = null;
            }

            boolean updated;
            try {
                updated = profileService.updateProfile(
                        userId,
                        name,
                        phoneNumber.isEmpty() ? null : phoneNumber,
                        description.isEmpty() ? null : description,
                        newImage);
            } catch (SQLException | RuntimeException e) {
                ImageStore.deleteQuietly(savedImage);
                throw e;
            }
            if (!updated) {
                ImageStore.deleteQuietly(savedImage);
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (!Objects.equals(oldImage, newImage)) {
                // Only files this app saved ("uploads/...") are deleted; anything else is left alone.
                ImageStore.deleteQuietly(oldImage);
            }

            AuthUtil.refreshProfileImage(request, newImage);
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
