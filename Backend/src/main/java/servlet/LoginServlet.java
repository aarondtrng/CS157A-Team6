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
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GET /login shows the form; POST /login checks the email and password. The "next" parameter (set by
 * LoginRequiredFilter) is validated by AuthUtil.safeNext before redirecting there.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(LoginServlet.class.getName());
    private static final String VIEW = "/WEB-INF/views/login.jsp";

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
        String email = authService.normalizeEmail(request.getParameter("email"));
        // Already hashed in the browser by js/auth.js; never the typed password.
        String clientHash = request.getParameter("password");

        request.setAttribute("next", next);
        request.setAttribute("email", email);

        if (!CsrfUtil.isValid(request)) {
            // Missing or wrong token: the form was stale (session expired) or came from another site.
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("error", "Your session expired. Please try again.");
            request.setAttribute("csrfToken", CsrfUtil.token(request));
            request.getRequestDispatcher(VIEW).forward(request, response);
            return;
        }
        request.setAttribute("csrfToken", CsrfUtil.token(request));

        try {
            Optional<User> user = authService.logIn(email, clientHash);
            if (user.isPresent()) {
                AuthUtil.logIn(request, response, user.get());
                response.sendRedirect(request.getContextPath() + next);
                return;
            }
            // Same message for unknown email and wrong password, so the form does not reveal who has an account.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            request.setAttribute("error", "Incorrect email or password.");
        } catch (UnsupportedOperationException e) {
            response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
            request.setAttribute("notImplemented", true);
        } catch (SQLException | IllegalStateException e) {
            LOG.log(Level.WARNING, "Login failed due to a database error", e);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            request.setAttribute("error", "Something went wrong. Please try again later.");
        }

        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
