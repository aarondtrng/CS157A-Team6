package dto;

/**
 * The raw text fields of the "post an item" form, exactly as typed (trimmed). Kept as strings so a form
 * with errors can be shown again with the same values. Plain bean so JSP EL can read it.
 */
public class PostForm {
    private final String type;
    private final String title;
    private final String description;
    private final String categoryId;
    private final String locationName;
    private final String latitude;
    private final String longitude;

    public PostForm(String type, String title, String description, String categoryId,
                    String locationName, String latitude, String longitude) {
        this.type = clean(type);
        this.title = clean(title);
        this.description = clean(description);
        this.categoryId = clean(categoryId);
        this.locationName = clean(locationName);
        this.latitude = clean(latitude);
        this.longitude = clean(longitude);
    }

    public static PostForm empty() {
        return new PostForm(null, null, null, null, null, null, null);
    }

    private static String clean(String value) {
        return value == null ? "" : value.strip();
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
