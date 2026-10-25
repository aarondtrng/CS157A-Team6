package service;

import dao.DataAccessException;
import dao.ItemDao;
import dto.NewPost;
import dto.PostForm;
import dto.PostResult;
import model.Category;
import util.ImageSniffer;
import util.ImageStore;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Validates a new post, stores its photos, and saves it through the DAO. */
public class PostService {
    public static final int MAX_IMAGES = 5;
    public static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;
    static final int MAX_TITLE_LENGTH = 255;
    static final int MAX_DESCRIPTION_LENGTH = 5000;
    static final int MAX_LOCATION_NAME_LENGTH = 255;
    static final int MAX_COORDINATE_TEXT_LENGTH = 20;

    // Items must be at or near the SJSU campus (about 1.5 km around it). The server enforces this even
    // though the map picker already limits clicks to the same area.
    static final double MIN_LATITUDE = 37.325;
    static final double MAX_LATITUDE = 37.345;
    static final double MIN_LONGITUDE = -121.895;
    static final double MAX_LONGITUDE = -121.870;

    private final ItemDao itemDao;

    public PostService(ItemDao itemDao) {
        this.itemDao = itemDao;
    }

    /** @throws DataAccessException if the database cannot be read */
    public List<Category> getCategories() {
        return itemDao.findAllCategories();
    }

    /**
     * Validates the form and photos; if everything is valid, stores the photos and saves the post.
     *
     * @return the new item id, or the field errors (nothing is stored when there are errors)
     * @throws DataAccessException if the database fails (any photos already stored are deleted again)
     * @throws UncheckedIOException if a photo cannot be written to the upload folder
     */
    public PostResult submit(int userId, PostForm form, List<byte[]> photos) {
        Map<String, String> errors = validate(form, itemDao.findAllCategories(), photos);
        if (!errors.isEmpty()) {
            return PostResult.failure(errors);
        }

        List<String> savedUrls = new ArrayList<>();
        try {
            for (byte[] photo : photos) {
                savedUrls.add(ImageStore.save(photo, ImageSniffer.extensionFor(photo)));
            }
            String locationName = form.getLocationName().isEmpty() ? null : form.getLocationName();
            NewPost post = new NewPost(userId, form.getType(), form.getTitle(), form.getDescription(),
                    Integer.parseInt(form.getCategoryId()), locationName,
                    coarsen(Double.parseDouble(form.getLatitude())),
                    coarsen(Double.parseDouble(form.getLongitude())), savedUrls);
            return PostResult.success(itemDao.createItem(post));
        } catch (IOException e) {
            savedUrls.forEach(ImageStore::deleteQuietly);
            throw new UncheckedIOException("Could not store a photo", e);
        } catch (RuntimeException e) {
            savedUrls.forEach(ImageStore::deleteQuietly);
            throw e;
        }
    }

    /**
     * Checks every field. Keys of the returned map are the field names the form shows errors under:
     * type, title, category, description, location, images. Empty when valid.
     */
    public Map<String, String> validate(PostForm form, List<Category> categories, List<byte[]> photos) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (!"LOST".equals(form.getType()) && !"FOUND".equals(form.getType())) {
            errors.put("type", "Choose whether the item was lost or found.");
        }

        if (form.getTitle().isEmpty()) {
            errors.put("title", "Enter a title.");
        } else if (form.getTitle().length() > MAX_TITLE_LENGTH) {
            errors.put("title", "Title must be at most " + MAX_TITLE_LENGTH + " characters.");
        }

        if (!isKnownCategory(form.getCategoryId(), categories)) {
            errors.put("category", "Choose a category.");
        }

        if (form.getDescription().isEmpty()) {
            errors.put("description", "Enter a description.");
        } else if (form.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            errors.put("description", "Description must be at most " + MAX_DESCRIPTION_LENGTH + " characters.");
        }

        if (form.getLocationName().length() > MAX_LOCATION_NAME_LENGTH) {
            errors.put("location", "Place name must be at most " + MAX_LOCATION_NAME_LENGTH + " characters.");
        } else {
            String locationError = checkCoordinates(form.getLatitude(), form.getLongitude());
            if (locationError != null) {
                errors.put("location", locationError);
            }
        }

        String imageError = checkPhotos(photos);
        if (imageError != null) {
            errors.put("images", imageError);
        }
        return errors;
    }

    private static boolean isKnownCategory(String text, List<Category> categories) {
        try {
            int id = Integer.parseInt(text);
            return categories.stream().anyMatch(c -> c.getCategoryId() == id);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Null when both coordinates are numbers inside the campus area; otherwise the message to show. */
    private static String checkCoordinates(String latitudeText, String longitudeText) {
        String pickPrompt = "Click the map to choose where the item was lost or found.";
        if (latitudeText.isEmpty() || longitudeText.isEmpty()) {
            return pickPrompt;
        }
        if (latitudeText.length() > MAX_COORDINATE_TEXT_LENGTH || longitudeText.length() > MAX_COORDINATE_TEXT_LENGTH) {
            return pickPrompt;
        }
        double latitude;
        double longitude;
        try {
            latitude = Double.parseDouble(latitudeText);
            longitude = Double.parseDouble(longitudeText);
        } catch (NumberFormatException e) {
            return pickPrompt;
        }
        if (!Double.isFinite(latitude) || !Double.isFinite(longitude)
                || latitude < MIN_LATITUDE || latitude > MAX_LATITUDE
                || longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
            return "Choose a spot on or near the SJSU campus.";
        }
        return null;
    }

    /** Null when the photos are acceptable (none is fine); otherwise the message to show. */
    private static String checkPhotos(List<byte[]> photos) {
        if (photos.size() > MAX_IMAGES) {
            return "You can add at most " + MAX_IMAGES + " photos.";
        }
        for (byte[] photo : photos) {
            if (photo.length > MAX_IMAGE_BYTES) {
                return "Each photo must be 5 MB or smaller.";
            }
            if (ImageSniffer.extensionFor(photo) == null) {
                return "Photos must be JPEG or PNG images.";
            }
        }
        return null;
    }

    /** Rounds to 3 decimals (about 100 m) so a precise spot is never stored. */
    static double coarsen(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
