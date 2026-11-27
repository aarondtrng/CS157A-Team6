package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Stops browsers from keeping copies of the app's pages.
 *
 * Every page shows who is logged in (the header avatar and account menu) and often private data such as
 * messages and requests. Without a Cache-Control header, a browser (Safari in particular) could show a copy
 * of a page saved while a different account was logged in, so one tab would briefly show another user's
 * avatar until the user navigated elsewhere. "no-store" makes the browser fetch each page fresh.
 *
 * Static files keep their own rules (StaticCacheFilter), and the photo servlets set their own
 * Cache-Control after this filter runs, which replaces the header set here.
 */
@WebFilter("/*")
public class NoStorePagesFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String path = ((HttpServletRequest) request).getServletPath();
        if (!isStaticFile(path)) {
            HttpServletResponse http = (HttpServletResponse) response;
            http.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, private");
            http.setHeader("Pragma", "no-cache");
            http.setDateHeader("Expires", 0);
        }
        chain.doFilter(request, response);
    }

    private static boolean isStaticFile(String path) {
        return path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/");
    }
}
