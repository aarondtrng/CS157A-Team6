package servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.AuthUtil;
import util.CsrfUtil;

import java.io.IOException;

/**
 * POST /logout ends the session. POST only, and the CSRF token must match, so neither a link, an image,
 * nor a form on another site can log users out.
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (AuthUtil.isLoggedIn(request) && !CsrfUtil.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        AuthUtil.logOut(request);
        response.sendRedirect(request.getContextPath() + AuthUtil.DEFAULT_AFTER_LOGIN);
    }
}
