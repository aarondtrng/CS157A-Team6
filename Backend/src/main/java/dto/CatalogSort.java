package dto;

/** Allowed catalog sort orders. Only these values ever reach the DAO. */
public enum CatalogSort {
    NEWEST("newest", "Newest first"),
    OLDEST("oldest", "Oldest first"),
    TITLE_ASC("az", "A–Z"),
    TITLE_DESC("za", "Z–A");

    private final String param;
    private final String label;

    CatalogSort(String param, String label) {
        this.param = param;
        this.label = label;
    }

    public String getParam() {
        return param;
    }

    public String getLabel() {
        return label;
    }

    /** Maps a request parameter to a sort order, defaulting to newest for missing or unknown values. */
    public static CatalogSort fromParam(String value) {
        for (CatalogSort sort : values()) {
            if (sort.param.equals(value)) {
                return sort;
            }
        }
        return NEWEST;
    }
}
