package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.AuthUtil;
import util.CsrfUtil;

import java.io.IOException;

/**
 * GET /logout shows a "Log out?" confirmation page (the landing page links here, because it cannot hold
 * the CSRF token). POST /logout ends the session: POST only, and the CSRF token must match, so neither a
 * link, an image, nor a form on another site can log users out.
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AuthUtil.isLoggedIn(request)) {
            // Already logged out (for example the session expired): drop any stale display cookie.
            AuthUtil.clearDisplayCookie(request, response);
            response.sendRedirect(request.getContextPath() + AuthUtil.DEFAULT_AFTER_LOGIN);
            return;
        }
        request.setAttribute("csrfToken", CsrfUtil.token(request));
        request.getRequestDispatcher("/WEB-INF/views/logout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (AuthUtil.isLoggedIn(request) && !CsrfUtil.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        AuthUtil.logOut(request, response);
        response.sendRedirect(request.getContextPath() + AuthUtil.DEFAULT_AFTER_LOGIN);
    }
}
