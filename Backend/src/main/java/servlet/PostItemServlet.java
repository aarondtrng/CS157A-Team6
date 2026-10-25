package servlet;

import dao.DataAccessException;
import dao.ItemDao;
import dto.PostForm;
import dto.PostResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import model.User;
import service.PostService;
import util.AuthUtil;
import util.CsrfUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * "Post an item": GET /post/new shows the form, POST /post/new saves the post and redirects to it.
 * Students only (checked here as well as by LoginRequiredFilter). The form is multipart because it
 * carries photos.
 */
@WebServlet("/post/new")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = PostService.MAX_IMAGE_BYTES,
        maxRequestSize = (long) PostService.MAX_IMAGE_BYTES * (PostService.MAX_IMAGES + 1))
public class PostItemServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(PostItemServlet.class.getName());
    private static final String VIEW = "/WEB-INF/views/post-form.jsp";
    private static final String PHOTO_FIELD = "images";

    private final PostService postService = new PostService(new ItemDao());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!allowed(request, response)) {
            return;
        }
        // /post/new?type=LOST (or FOUND) opens the form with that choice already selected.
        String type = request.getParameter("type");
        PostForm form = "LOST".equals(type) || "FOUND".equals(type)
                ? new PostForm(type, null, null, null, null, null, null)
                : PostForm.empty();
        render(request, response, form, Map.of(), HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!allowed(request, response)) {
            return;
        }
        int userId = AuthUtil.currentUserId(request);

        // Read the photos first: reading the form fields also parses the multipart body.
        List<byte[]> photos = new ArrayList<>();
        try {
            for (Part part : request.getParts()) {
                if (PHOTO_FIELD.equals(part.getName()) && part.getSize() > 0) {
                    if (photos.size() > PostService.MAX_IMAGES) {
                        break; // Too many: the service reports it. Do not read more.
                    }
                    try (InputStream in = part.getInputStream()) {
                        photos.add(in.readNBytes(PostService.MAX_IMAGE_BYTES + 1));
                    }
                }
            }
        } catch (IllegalStateException | IOException | ServletException e) {
            // A file or the whole request exceeded the size limits.
            render(request, response, PostForm.empty(),
                    Map.of("images", "Each photo must be 5 MB or smaller, and you can add at most "
                            + PostService.MAX_IMAGES + " photos."),
                    HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        PostForm form = new PostForm(
                request.getParameter("type"),
                request.getParameter("title"),
                request.getParameter("description"),
                request.getParameter("categoryId"),
                request.getParameter("locationName"),
                request.getParameter("latitude"),
                request.getParameter("longitude"));

        if (!CsrfUtil.isValid(request)) {
            // Missing or wrong token: a stale form (session expired) or one submitted from another site.
            render(request, response, form,
                    Map.of("form", "Your session expired. Please check your post and submit it again."),
                    HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            PostResult result = postService.submit(userId, form, photos);
            if (result.isSuccess()) {
                // Post/Redirect/Get: refreshing the next page cannot submit the post twice.
                response.sendRedirect(request.getContextPath() + "/item?id=" + result.itemId());
                return;
            }
            render(request, response, form, result.errors(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (DataAccessException | UncheckedIOException e) {
            LOG.log(Level.SEVERE, "Post could not be saved", e);
            render(request, response, form,
                    Map.of("form", "We could not save your post. Please try again in a moment."),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /** True for a logged-in student; otherwise sends guests to log in and everyone else a 403. */
    private boolean allowed(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer userId = AuthUtil.currentUserId(request);
        if (userId == null) {
            String next = URLEncoder.encode(AuthUtil.currentPathWithQuery(request), StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + AuthUtil.LOGIN_PATH + "?next=" + next);
            return false;
        }
        if (!User.ROLE_STUDENT.equals(AuthUtil.currentRole(request))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    private void render(HttpServletRequest request, HttpServletResponse response, PostForm form,
                        Map<String, String> errors, int status) throws ServletException, IOException {
        try {
            request.setAttribute("categories", postService.getCategories());
        } catch (DataAccessException e) {
            LOG.log(Level.SEVERE, "Categories could not be loaded for the post form", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        request.setAttribute("form", form);
        request.setAttribute("errors", new LinkedHashMap<>(errors));
        request.setAttribute("csrfToken", CsrfUtil.token(request));
        request.setAttribute("maxImages", PostService.MAX_IMAGES);
        response.setStatus(status);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
