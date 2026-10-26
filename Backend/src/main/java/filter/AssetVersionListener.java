package filter;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Gives every page a version number for its stylesheet and script addresses ("catalog.css?v=...").
 * The number is the time the app started, so it changes on every redeploy. A browser that cached an old
 * copy of a file sees a new address and fetches the new file, with no need for a manual hard refresh.
 */
@WebListener
public class AssetVersionListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        event.getServletContext().setAttribute("assetVersion", Long.toString(System.currentTimeMillis()));
    }
}
