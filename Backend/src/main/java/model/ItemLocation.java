package model;

/**
 * Where an item was lost or found (not the poster's position). Coordinates are rounded on
 * construction (4 decimals, about 10 m), so values more precise than that never reach a page. Older posts
 * saved with 3 decimals (about 100 m) simply keep their coarser values.
 */
public class ItemLocation {
    private static final double ROUNDING = 10000.0;

    private final String locationName;
    private final double latitude;
    private final double longitude;

    public ItemLocation(String locationName, double latitude, double longitude) {
        this.locationName = locationName;
        this.latitude = Math.round(latitude * ROUNDING) / ROUNDING;
        this.longitude = Math.round(longitude * ROUNDING) / ROUNDING;
    }

    public String getLocationName() {
        return locationName;
    }

    /** Approximate (rounded) latitude. */
    public double getLatitude() {
        return latitude;
    }

    /** Approximate (rounded) longitude. */
    public double getLongitude() {
        return longitude;
    }
}
