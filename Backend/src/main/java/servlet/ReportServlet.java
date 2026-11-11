package servlet;

import dao.DataAccessException;
import dao.ReportDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.ReportTarget;
import model.User;
import service.ReportService;
import util.AuthUtil;
import util.CsrfUtil;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Submission form and administrator-only report queue/actions. */
@WebServlet({"/report/new", "/admin/reports", "/admin/reports/action"})
public class ReportServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(ReportServlet.class.getName());
    private final ReportService service = new ReportService(new ReportDao());

    private boolean allowed(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control", "no-store");
        if (AuthUtil.currentUserId(request) == null) {
            String next = URLEncoder.encode(AuthUtil.currentPathWithQuery(request), StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + AuthUtil.LOGIN_PATH + "?next=" + next);
            return false;
        }
        String role = AuthUtil.currentRole(request);
        boolean adminPage = request.getServletPath().startsWith("/admin/");
        if (!(adminPage ? User.ROLE_ADMINISTRATOR.equals(role)
                : User.ROLE_STUDENT.equals(role) || User.ROLE_ADMINISTRATOR.equals(role))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!allowed(request, response)) { return; }
        if ("/admin/reports/action".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        if ("/admin/reports".equals(request.getServletPath())) {
            renderQueue(request, response);
        } else {
            renderForm(request, response, false);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!allowed(request, response)) { return; }
        if (!CsrfUtil.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if ("/report/new".equals(request.getServletPath())) {
            renderForm(request, response, true);
            return;
        }
        if (!"/admin/reports/action".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        Integer reportId = ReportService.parseId(request.getParameter("reportId"));
        String action = request.getParameter("action");
        if (reportId == null || !("resolve".equals(action) || "dismiss".equals(action))) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            boolean changed = service.moderate(AuthUtil.currentRole(request), reportId, action);
            request.getSession().setAttribute("reportQueueMessage", changed
                    ? "Report updated." : "This report was already reviewed or could not be found.");
            response.sendRedirect(request.getContextPath() + "/admin/reports?status="
                    + ReportService.filter(request.getParameter("status")));
        } catch (DataAccessException e) {
            databaseFailure(request, response, e);
            renderQueue(request, response);
        }
    }

    private void renderForm(HttpServletRequest request, HttpServletResponse response, boolean submitting)
            throws ServletException, IOException {
        String type = request.getParameter("type");
        Integer id = ReportService.parseId(request.getParameter("id"));
        if (id == null || !("item".equals(type) || "profile".equals(type))) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String reason = request.getParameter("reason");
        request.setAttribute("reason", reason == null ? "" : reason);
        request.setAttribute("targetType", type);
        request.setAttribute("targetId", id);
        request.setAttribute("maxLength", ReportService.MAX_REASON_LENGTH);
        try {
            Optional<ReportTarget> found = service.findTarget(type, id);
            if (found.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            ReportTarget target = found.get();
            request.setAttribute("target", target);
            if (target.getUserId() == AuthUtil.currentUserId(request)) {
                request.setAttribute("blockedReason", "You cannot report your own profile or post.");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            } else if (submitting) {
                String error = ReportService.reasonError(ReportService.normalizeReason(reason));
                if (error != null) {
                    request.setAttribute("reasonError", error);
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                } else if (service.submit(AuthUtil.currentUserId(request), target, reason)) {
                    request.getSession().setAttribute("reportSent", true);
                    response.sendRedirect(request.getContextPath() + "/report/new?type=" + type + "&id=" + id);
                    return;
                } else {
                    request.setAttribute("error", "Your report could not be saved. Please try again.");
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                }
            }
        } catch (DataAccessException e) {
            databaseFailure(request, response, e);
        }
        request.setAttribute("sent", Boolean.TRUE.equals(request.getSession().getAttribute("reportSent")));
        request.getSession().removeAttribute("reportSent");
        request.setAttribute("csrfToken", CsrfUtil.token(request));
        request.getRequestDispatcher("/WEB-INF/views/report-form.jsp").forward(request, response);
    }

    private void renderQueue(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String filter = ReportService.filter(request.getParameter("status"));
        request.setAttribute("statusFilter", filter);
        // After a failed update, keep its error and do not attempt another database operation.
        if (request.getAttribute("error") == null) {
            try {
                request.setAttribute("reports", service.list(AuthUtil.currentRole(request), filter));
            } catch (DataAccessException e) {
                databaseFailure(request, response, e);
            }
        }
        request.setAttribute("queueMessage", request.getSession().getAttribute("reportQueueMessage"));
        request.getSession().removeAttribute("reportQueueMessage");
        request.setAttribute("csrfToken", CsrfUtil.token(request));
        request.getRequestDispatcher("/WEB-INF/views/reports.jsp").forward(request, response);
    }

    private void databaseFailure(HttpServletRequest request, HttpServletResponse response, DataAccessException e) {
        LOG.log(e instanceof ReportDao.SqlNotConfiguredException ? Level.WARNING : Level.SEVERE,
                "Report operation unavailable", e);
        response.setStatus(e instanceof ReportDao.SqlNotConfiguredException
                ? HttpServletResponse.SC_SERVICE_UNAVAILABLE : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        request.setAttribute("error", "Reports are temporarily unavailable. Please try again later.");
    }
}
