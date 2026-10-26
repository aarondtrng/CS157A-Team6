package servlet;

import dao.UserDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import service.AuthService;
import util.AuthUtil;
import util.CsrfUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GET /signup shows the form; POST /signup creates a student account with an SJSU email and logs the new
 * student in. The "password" parameter is the browser-side hash from js/auth.js, not the typed password.
 */
@WebServlet("/signup")
public class SignupServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(SignupServlet.class.getName());
    private static final String VIEW = "/WEB-INF/views/signup.jsp";

    private final AuthService authService = new AuthService(new UserDao());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String next = AuthUtil.safeNext(request.getParameter("next"));
        if (AuthUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + next);
            return;
        }
        request.setAttribute("next", next);
        request.setAttribute("csrfToken", CsrfUtil.token(request));
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String next = AuthUtil.safeNext(request.getParameter("next"));
        String firstName = authService.normalizeNamePart(request.getParameter("firstName"));
        String lastName = authService.normalizeNamePart(request.getParameter("lastName"));
        String email = authService.normalizeEmail(request.getParameter("email"));
        // Already hashed in the browser by js/auth.js; never the typed password.
        String clientHash = request.getParameter("password");

        request.setAttribute("next", next);
        request.setAttribute("firstName", firstName);
        request.setAttribute("lastName", lastName);
        request.setAttribute("email", email);

        if (!CsrfUtil.isValid(request)) {
            // Missing or wrong token: the form was stale (session expired) or came from another site.
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("errors", List.of("Your session expired. Please try again."));
            request.setAttribute("csrfToken", CsrfUtil.token(request));
            request.getRequestDispatcher(VIEW).forward(request, response);
            return;
        }
        request.setAttribute("csrfToken", CsrfUtil.token(request));

        List<String> errors = authService.validateSignup(firstName, lastName, email, clientHash);
        if (!errors.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("errors", errors);
            request.getRequestDispatcher(VIEW).forward(request, response);
            return;
        }

        try {
            Optional<Integer> userId = authService.signUp(firstName, lastName, email, clientHash);
            if (userId.isPresent()) {
                AuthUtil.logIn(request, response, new User(userId.get(), authService.fullName(firstName, lastName), email, null, User.ROLE_STUDENT));
                // TODO: redirect to the profile edit page once it exists (design: sign up leads there).
                response.sendRedirect(request.getContextPath() + next);
                return;
            }
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            request.setAttribute("errors", List.of("An account with this email already exists. Try logging in."));
        } catch (UnsupportedOperationException e) {
            // DAO SQL has not been written yet: say so instead of pretending the account was created.
            response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
            request.setAttribute("notImplemented", true);
        } catch (SQLException | IllegalStateException e) {
            LOG.log(Level.WARNING, "Sign up failed due to a database error", e);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            request.setAttribute("errors", List.of("Something went wrong. Please try again later."));
        }

        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
