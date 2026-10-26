package model;

/**
 * Where an item was lost or found (not the poster's position), exactly as the poster marked it on the map.
 * Coordinates are not rounded.
 */
public class ItemLocation {
    private final String locationName;
    private final double latitude;
    private final double longitude;

    public ItemLocation(String locationName, double latitude, double longitude) {
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLocationName() {
        return locationName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
