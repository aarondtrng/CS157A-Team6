package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.AuthUtil;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Sends guests to the login page before they can open a protected page.
 *
 * NOT MAPPED to any URL right now: guests may view public posts (/items, /item), so nothing is
 * protected yet. When pages that need an account exist (posting, requests, messaging, reports), map this
 * filter to them with @WebFilter(...) or a web.xml entry. Also enforce the role in the servlet, not only here.
 *
 * Original behavior: sends guests to the login page before they can open a protected page. The original path is passed as
 * the "next" parameter so login can return them afterwards.
 *
 * The login page validates "next" itself: it must accept only paths inside this app.
 */
public class LoginRequiredFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (AuthUtil.isLoggedIn(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        String next = URLEncoder.encode(AuthUtil.currentPathWithQuery(httpRequest), StandardCharsets.UTF_8);
        httpResponse.sendRedirect(httpRequest.getContextPath() + AuthUtil.LOGIN_PATH + "?next=" + next);
    }
}
