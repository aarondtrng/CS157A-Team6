package service;

import dao.ReportDao;
import model.ReportRow;
import model.ReportTarget;
import model.User;

import java.util.List;
import java.util.Optional;

/** Run main after mvn test-compile; no database or test framework required. */
public class ReportServiceTest {
    private static int checks;

    public static void main(String[] args) {
        FakeDao dao = new FakeDao();
        ReportService service = new ReportService(dao);
        ReportTarget item = new ReportTarget(7, 2, "Item");
        ReportTarget profile = new ReportTarget(null, 2, "Student");

        check(ReportService.parseId(" 42 ") == 42, "positive ID");
        for (String invalid : new String[] {null, "", "0", "-1", "abc", "2147483648"}) {
            check(ReportService.parseId(invalid) == null, "reject invalid ID");
        }
        check(ReportService.normalizeReason("  a\r\nb\rc  ").equals("a\nb\nc"), "normalize reason");
        for (String reason : new String[] {"", " \n\t", "x".repeat(5001), "bad\u0000text"}) {
            check(!service.submit(1, item, reason), "reject invalid reason");
        }
        check(dao.creates == 0, "invalid submissions never reach persistence");
        check(!service.submit(2, item, "Abusive post"), "no self-reporting items");
        check(!service.submit(2, profile, "Abusive profile"), "no self-reporting profiles");
        check(service.submit(1, item, "  Abusive\r\npost  "), "submit item");
        check(dao.reason.equals("Abusive\npost") && dao.target == item && dao.reporter == 1,
                "persist normalized reason and server target");
        check(service.submit(1, profile, "x".repeat(5000)), "maximum length and profile target");
        check(dao.target.getItemId() == null, "profile reports have no item ID");
        dao.saved = false;
        check(!service.submit(1, item, "Abusive post"), "failed insert is not success");
        check(service.findTarget("item", 7).get() == dao.item, "item lookup");
        check(service.findTarget("profile", 2).get() == dao.profile, "profile lookup");
        check(service.findTarget("other", 7).isEmpty(), "unknown target rejected");
        check(ReportService.filter("RESOLVED").equals("RESOLVED"), "known filter");
        check(ReportService.filter(null).equals("PENDING"), "default filter");
        check(ReportService.filter("injected").equals("PENDING"), "unknown filter");
        for (String role : new String[] {null, User.ROLE_STUDENT, "ADMIN"}) {
            try { service.list(role, "ALL"); throw new AssertionError("queue exposed"); }
            catch (SecurityException expected) { checks++; }
            try { service.moderate(role, 1, "resolve"); throw new AssertionError("moderation exposed"); }
            catch (SecurityException expected) { checks++; }
        }
        service.list(User.ROLE_ADMINISTRATOR, "ALL");
        check(dao.filter.equals("ALL"), "administrator filter");
        check(!service.moderate(User.ROLE_ADMINISTRATOR, 1, "reopen"), "unknown action");
        check(!service.moderate(User.ROLE_ADMINISTRATOR, 0, "resolve"), "invalid report ID");
        check(service.moderate(User.ROLE_ADMINISTRATOR, 1, "resolve")
                && dao.status.equals("RESOLVED"), "resolve status");
        check(service.moderate(User.ROLE_ADMINISTRATOR, 1, "dismiss")
                && dao.status.equals("DISMISSED"), "dismiss status");
        dao.changed = false;
        check(!service.moderate(User.ROLE_ADMINISTRATOR, 1, "resolve"), "stale decision not success");

        System.out.println("Report service checks passed: " + checks);
    }

    private static void check(boolean success, String message) {
        if (!success) { throw new AssertionError(message); }
        checks++;
    }

    private static class FakeDao extends ReportDao {
        final ReportTarget item = new ReportTarget(7, 2, "Item");
        final ReportTarget profile = new ReportTarget(null, 2, "Student");
        int creates;
        int reporter;
        ReportTarget target;
        String reason;
        String filter;
        String status;
        boolean saved = true;
        boolean changed = true;
        public Optional<ReportTarget> findItem(int id) { return Optional.of(item); }
        public Optional<ReportTarget> findProfile(int id) { return Optional.of(profile); }
        public boolean create(int reporter, ReportTarget target, String reason) {
            creates++; this.reporter = reporter; this.target = target; this.reason = reason; return saved;
        }
        public List<ReportRow> findReports(String filter) { this.filter = filter; return List.of(); }
        public boolean updateStatus(int id, String status) { this.status = status; return changed; }
    }
}
