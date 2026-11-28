package service;

import dao.ReportDao;
import dao.ItemDao;
import model.ReportRow;
import model.ReportTarget;
import model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Validation and moderation rules; report text is private to administrators. */
public class ReportService {
    public static final int MAX_REASON_LENGTH = 5000;
    private static final Set<String> FILTERS = Set.of("ALL", "PENDING", "RESOLVED", "DISMISSED");
    private final ReportDao dao;
    private final ItemDao itemDao;

    public ReportService(ReportDao dao) {
        this.dao = dao;
        this.itemDao = new ItemDao();
    }

    public static Integer parseId(String raw) {
        if (raw == null) { return null; }
        try {
            int id = Integer.parseInt(raw.strip());
            return id > 0 ? id : null;
        } catch (NumberFormatException e) { return null; }
    }

    public static String normalizeReason(String raw) {
        return raw == null ? "" : raw.replace("\r\n", "\n").replace('\r', '\n').strip();
    }

    public static String reasonError(String reason) {
        if (reason.isBlank()) { return "Please explain why you are reporting this content."; }
        if (reason.length() > MAX_REASON_LENGTH) { return "Please keep your reason to 5000 characters or fewer."; }
        if (reason.chars().anyMatch(c -> Character.isISOControl(c) && c != '\n' && c != '\t')) {
            return "The reason contains characters that are not allowed.";
        }
        return null;
    }

    public Optional<ReportTarget> findTarget(String type, int id) {
        if ("item".equals(type)) { return dao.findItem(id); }
        if ("profile".equals(type)) { return dao.findProfile(id); }
        return Optional.empty();
    }

    public boolean submit(int reporterId, ReportTarget target, String rawReason) {
        String reason = normalizeReason(rawReason);
        if (target.getUserId() == reporterId || reasonError(reason) != null) { return false; }
        return dao.create(reporterId, target, reason);
    }

    public static String filter(String raw) { return FILTERS.contains(raw == null ? "" : raw) ? raw : "PENDING"; }

    public List<ReportRow> list(String role, String filter) {
        requireAdministrator(role);
        return dao.findReports(filter(filter));
    }

    public boolean moderate(String role, int reportId, String action) {
        requireAdministrator(role);
        if (reportId <= 0) { return false; }
        if ("resolve".equals(action)) { return dao.updateStatus(reportId, "RESOLVED"); }
        if ("dismiss".equals(action)) { return dao.updateStatus(reportId, "DISMISSED"); }
        return false;
    }

    public boolean removeItem(String role, int reportId) {
        requireAdministrator(role);

        if (reportId <= 0) {
            return false;
        }

        Optional<Integer> itemId =
            dao.findItemIdByReport(reportId);

        if (itemId.isEmpty()) {
            return false;
        }

        if (!itemDao.removeItem(itemId.get())) {
            return false;
        }

        // Removing the post settles every pending report about it, not just this one.
        return dao.updateStatusByItem(itemId.get(), "RESOLVED") > 0;
    }

    private static void requireAdministrator(String role) {
        if (!User.ROLE_ADMINISTRATOR.equals(role)) { throw new SecurityException("Administrator required"); }
    }
}
