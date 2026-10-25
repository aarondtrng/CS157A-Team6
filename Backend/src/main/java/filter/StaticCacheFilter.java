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
 * Makes browsers re-check the site's CSS, JavaScript and images on every page load.
 *
 * Files inside a WAR carry a fixed 1980 modification date, which lets browsers keep their copy for years
 * without asking again, so a changed stylesheet or script would not show up after a redeploy. With
 * "no-cache" the browser still keeps its copy but asks the server first; an unchanged file costs only a
 * short "not modified" reply. (Uploaded photos use UploadServlet, which has its own caching.)
 */
@WebFilter({"/css/*", "/js/*", "/images/*"})
public class StaticCacheFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        ((HttpServletResponse) response).setHeader("Cache-Control", "no-cache");
        chain.doFilter(request, response);
    }
}
