package servlet;

import filter.LoginRequiredFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;

/** Standalone regression check: guest reports must stay inside the deployed app when going to login. */
public class ReportNavigationChecks extends ReportServlet {
    public static void main(String[] args) throws Exception {
        int checks = 0;
        for (String context : new String[] {"", "/spartanfinds", "/nested/app"}) {
            for (String type : new String[] {"item", "profile"}) {
                String query = "type=" + type + "&id=7";
                HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                        ReportNavigationChecks.class.getClassLoader(), new Class<?>[] {HttpServletRequest.class},
                        (proxy, method, values) -> switch (method.getName()) {
                            case "getContextPath" -> context;
                            case "getRequestURI" -> context + "/report/new";
                            case "getServletPath" -> "/report/new";
                            case "getQueryString" -> query;
                            case "getSession" -> null;
                            default -> null;
                        });
                AtomicReference<String> location = new AtomicReference<>();
                HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                        ReportNavigationChecks.class.getClassLoader(), new Class<?>[] {HttpServletResponse.class},
                        (proxy, method, values) -> {
                            if ("sendRedirect".equals(method.getName())) { location.set((String) values[0]); }
                            return null;
                        });

                new LoginRequiredFilter().doFilter(request, response, (req, res) -> {
                    throw new AssertionError("Guest request reached protected page");
                });
                verify(location.get(), context, query);
                checks++;
                location.set(null);
                new ReportNavigationChecks().doGet(request, response);
                verify(location.get(), context, query);
                checks++;
            }
        }
        for (String role : new String[] {"STUDENT", "ADMIN", "ADMINISTRATOR"}) {
            HttpSession session = (HttpSession) Proxy.newProxyInstance(
                    ReportNavigationChecks.class.getClassLoader(), new Class<?>[] {HttpSession.class},
                    (proxy, method, values) -> {
                        if (!"getAttribute".equals(method.getName())) { return null; }
                        return "userId".equals(values[0]) ? 1 : "userRole".equals(values[0]) ? role : null;
                    });
            HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                    ReportNavigationChecks.class.getClassLoader(), new Class<?>[] {HttpServletRequest.class},
                    (proxy, method, values) -> switch (method.getName()) {
                        case "getSession" -> session;
                        case "getServletPath" -> "/admin/reports/action";
                        default -> null;
                    });
            AtomicInteger status = new AtomicInteger();
            HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                    ReportNavigationChecks.class.getClassLoader(), new Class<?>[] {HttpServletResponse.class},
                    (proxy, method, values) -> {
                        if ("sendError".equals(method.getName())) { status.set((Integer) values[0]); }
                        return null;
                    });
            new ReportNavigationChecks().doGet(request, response);
            // Administrators pass authorization but GET cannot perform a moderation action.
            int expected = "ADMINISTRATOR".equals(role) ? 405 : 403;
            if (status.get() != expected) { throw new AssertionError("Unexpected access for " + role); }
            checks++;
        }
        System.out.println("Report navigation and administrator access checks passed: " + checks);
    }

    private static void verify(String location, String context, String query) {
        if (location == null) { throw new AssertionError("No login redirect"); }
        URI uri = URI.create(location);
        if (!uri.getPath().equals(context + "/login")) {
            throw new AssertionError("Wrong login path: " + location);
        }
        String next = URLDecoder.decode(uri.getRawQuery().substring("next=".length()), StandardCharsets.UTF_8);
        if (!next.equals("/report/new?" + query)) {
            throw new AssertionError("Report target was not preserved: " + next);
        }
    }
}
