package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Adds browser security headers to every response. The main one stops other sites from showing a
 * SpartanFinds page inside a frame (clickjacking: tricking a click on a hidden Log out or Request button).
 * The app has no reason to be framed, so framing is denied outright.
 *
 * The referrer policy must still send this site's address to other sites: OpenStreetMap's tile servers
 * block requests that arrive without a Referer and return a "tile usage policy" image instead of the map.
 */
@WebFilter("/*")
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse http = (HttpServletResponse) response;
        http.setHeader("X-Frame-Options", "DENY");
        http.setHeader("Content-Security-Policy", "frame-ancestors 'none'");
        http.setHeader("X-Content-Type-Options", "nosniff");
        http.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        chain.doFilter(request, response);
    }
}
