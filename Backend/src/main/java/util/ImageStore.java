package util;

import config.UploadConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Saves and finds uploaded photos in the upload folder. Files get a random name (never the uploader's),
 * and the stored image_url is "uploads/<name>", which UploadServlet serves.
 */
public final class ImageStore {
    public static final String URL_PREFIX = "uploads/";
    /** Message photos live in their own subfolder and are only served to the two people in the conversation. */
    public static final String MESSAGE_PREFIX = "message-uploads/";
    private static final String MESSAGE_DIR = "messages";
    private static final Pattern SAFE_NAME =
            Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png)");

    private ImageStore() {
    }

    /** Writes the bytes to a new randomly named file and returns its image_url ("uploads/<name>"). */
    public static String save(byte[] data, String extension) throws IOException {
        Path dir = UploadConfig.uploadDir();
        Files.createDirectories(dir);
        String name = UUID.randomUUID() + "." + extension;
        Files.write(dir.resolve(name), data, StandardOpenOption.CREATE_NEW);
        return URL_PREFIX + name;
    }

    /** Like save(), but for a message photo: returns its image_url ("message-uploads/<name>"). */
    public static String saveMessagePhoto(byte[] data, String extension) throws IOException {
        Path dir = UploadConfig.uploadDir().resolve(MESSAGE_DIR);
        Files.createDirectories(dir);
        String name = UUID.randomUUID() + "." + extension;
        Files.write(dir.resolve(name), data, StandardOpenOption.CREATE_NEW);
        return MESSAGE_PREFIX + name;
    }

    /** The file for a stored message photo name, or null unless it is exactly one of our generated names. */
    public static Path resolveMessagePhoto(String fileName) {
        if (fileName == null || !SAFE_NAME.matcher(fileName).matches()) {
            return null;
        }
        return UploadConfig.uploadDir().resolve(MESSAGE_DIR).resolve(fileName);
    }

    /** Deletes a file saved by save(); ignores anything else and any failure. */
    public static void deleteQuietly(String imageUrl) {
        if (imageUrl == null) {
            return;
        }
        Path path;
        if (imageUrl.startsWith(URL_PREFIX)) {
            path = resolve(imageUrl.substring(URL_PREFIX.length()));
        } else if (imageUrl.startsWith(MESSAGE_PREFIX)) {
            path = resolveMessagePhoto(imageUrl.substring(MESSAGE_PREFIX.length()));
        } else {
            return;
        }
        if (path != null) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
                // Best effort: a leftover file is harmless.
            }
        }
    }

    /**
     * The file for a stored name, or null unless the name is exactly one of our generated names. Because
     * only that fixed shape is accepted, "../" and other path tricks can never reach another file.
     */
    public static Path resolve(String fileName) {
        if (fileName == null || !SAFE_NAME.matcher(fileName).matches()) {
            return null;
        }
        return UploadConfig.uploadDir().resolve(fileName);
    }
}
