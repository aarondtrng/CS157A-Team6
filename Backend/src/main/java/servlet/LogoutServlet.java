package servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.AuthUtil;

import java.io.IOException;

/** POST /logout ends the session. POST only, so a link or image on another site cannot log users out. */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        AuthUtil.logOut(request);
        response.sendRedirect(request.getContextPath() + AuthUtil.DEFAULT_AFTER_LOGIN);
    }
}
