package demo;

import dao.ItemDao;
import dto.CatalogQuery;
import model.Category;
import model.ItemSummary;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * DEMO ONLY -- FAKE DATA. Stands in for ItemDao so the catalog page can be viewed without a database.
 * Nothing here touches MySQL or JDBC, and none of these items exist anywhere. It is reachable only
 * through /items?demo=1 from the local machine (see CatalogServlet). The real ItemDao is unchanged and
 * still throws UnsupportedOperationException. Delete this class once the real DAO SQL is written.
 */
public class MockItemDao extends ItemDao {
    private static final List<Category> CATEGORIES = List.of(
            new Category(1, "Bags & Backpacks"),
            new Category(2, "Electronics"),
            new Category(3, "Keys & Cards"),
            new Category(4, "Clothing"),
            new Category(5, "Water Bottles"));

    private static final List<ItemSummary> ITEMS = buildItems();

    @Override
    public List<ItemSummary> searchPublicItems(CatalogQuery query) {
        List<ItemSummary> matches = new ArrayList<>(filter(query));
        matches.sort(comparatorFor(query));
        int from = Math.min(query.getOffset(), matches.size());
        int to = Math.min(from + query.getPageSize(), matches.size());
        return new ArrayList<>(matches.subList(from, to));
    }

    @Override
    public int countPublicItems(CatalogQuery query) {
        return filter(query).size();
    }

    @Override
    public List<Category> findAllCategories() {
        return CATEGORIES;
    }

    private static List<ItemSummary> filter(CatalogQuery query) {
        String needle = query.getSearchText().toLowerCase(Locale.ROOT);
        String categoryName = null;
        if (query.getCategoryId() != null) {
            for (Category c : CATEGORIES) {
                if (c.getCategoryId() == query.getCategoryId()) {
                    categoryName = c.getCategoryName();
                }
            }
            if (categoryName == null) {
                return List.of();
            }
        }
        List<ItemSummary> result = new ArrayList<>();
        for (ItemSummary item : ITEMS) {
            boolean textMatch = needle.isEmpty()
                    || item.getTitle().toLowerCase(Locale.ROOT).contains(needle)
                    || item.getDescription().toLowerCase(Locale.ROOT).contains(needle);
            boolean categoryMatch = categoryName == null || categoryName.equals(item.getCategoryName());
            if (textMatch && categoryMatch) {
                result.add(item);
            }
        }
        return result;
    }

    private static Comparator<ItemSummary> comparatorFor(CatalogQuery query) {
        Comparator<ItemSummary> byTitle = Comparator.comparing(i -> i.getTitle().toLowerCase(Locale.ROOT));
        Comparator<ItemSummary> byDate = Comparator.comparing(ItemSummary::getCreatedDateText)
                .thenComparingInt(ItemSummary::getItemId);
        return switch (query.getSort()) {
            case NEWEST -> byDate.reversed();
            case OLDEST -> byDate;
            case TITLE_ASC -> byTitle;
            case TITLE_DESC -> byTitle.reversed();
        };
    }

    private static List<ItemSummary> buildItems() {
        LocalDateTime base = LocalDateTime.of(2026, 10, 1, 9, 0);
        String[][] rows = {
                {"LOST", "Black Jansport backpack", "Bags & Backpacks", "Black backpack with a red keychain on the zipper. Contains a calculus textbook. Lost near the Engineering building."},
                {"FOUND", "Blue Hydro Flask", "Water Bottles", "32 oz blue Hydro Flask with stickers on one side. Found on a table in the Student Union."},
                {"LOST", "AirPods Pro case", "Electronics", "White AirPods Pro charging case with a small scratch on the lid. Last seen in the Martin Luther King Jr. Library."},
                {"FOUND", "Student ID card", "Keys & Cards", "SJSU student ID found on the ground outside the Event Center. Can be claimed by describing the photo."},
                {"LOST", "Gray hoodie", "Clothing", "Gray zip-up hoodie, size medium, left in a classroom in Boccardo Business Complex."},
                {"FOUND", "Set of keys with lanyard", "Keys & Cards", "Three keys on a gold SJSU lanyard. Found near the 7th Street garage."},
                {"LOST", "TI-84 graphing calculator", "Electronics", "Calculator with initials written on the back in marker. Lost after a midterm in Dwight Bentel Hall."},
                {"FOUND", "Green water bottle", "Water Bottles", "Green plastic bottle with a dent near the bottom. Found beside the Clark Hall entrance."},
                {"LOST", "Navy blue umbrella", "Clothing", "Compact navy umbrella with a wooden handle. Probably left on a VTA bus stop bench on San Fernando Street."},
                {"FOUND", "Laptop charger", "Electronics", "USB-C laptop charger, 65W, found plugged in at a study table on the third floor of the library."},
                {"LOST", "Brown leather wallet", "Keys & Cards", "Brown leather wallet with no cash. Lost somewhere between the Student Union and the garage."},
                {"FOUND", "Red North Face jacket", "Clothing", "Red jacket, women's small, found draped over a chair in the Dining Commons."},
                {"LOST", "Purple gym bag", "Bags & Backpacks", "Purple duffel bag with a towel and running shoes inside. Lost near the Spartan Recreation and Aquatic Center."},
                {"FOUND", "Wireless mouse", "Electronics", "Black Logitech wireless mouse found in a computer lab in the Engineering building."},
                {"LOST", "Silver water bottle", "Water Bottles", "Insulated silver bottle with a black lid and a small sticker of a cat."},
                {"FOUND", "Prescription glasses", "Clothing", "Black-framed prescription glasses in a gray case, found on a bench at Tower Lawn."},
                {"LOST", "Car key fob", "Keys & Cards", "Single Honda key fob with a blue rubber cover. Lost on the walkway near the Art building."},
                {"FOUND", "Canvas tote bag", "Bags & Backpacks", "Beige canvas tote with notebooks inside. Found in the lobby of Sweeney Hall."},
        };
        List<ItemSummary> items = new ArrayList<>();
        for (int i = 0; i < rows.length; i++) {
            items.add(new ItemSummary(i + 1, rows[i][1], rows[i][3], rows[i][0], "ACTIVE", rows[i][2],
                    base.plusDays(i), null));
        }
        return List.copyOf(items);
    }
}
