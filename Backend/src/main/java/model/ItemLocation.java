package model;

/**
 * Where an item was lost or found (not the poster's position). Coordinates are rounded on
 * construction (3 decimals, about 100 m), so precise values never reach a page.
 */
public class ItemLocation {
    private static final double ROUNDING = 1000.0;

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
