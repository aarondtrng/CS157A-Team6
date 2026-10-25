package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.AuthUtil;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Sends guests to the login page before they can open a protected page. The original path is passed as
 * the "next" parameter so login can return them afterwards (AuthUtil.safeNext validates it).
 *
 * Mapped to the pages that need an account. Guests may still view /items and /item. When a new page
 * needs an account (requests, messaging, reports), add its URL here. The servlet must also check the
 * user and role itself, because this filter only covers the URLs listed.
 */
@WebFilter({"/post/new"})
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
