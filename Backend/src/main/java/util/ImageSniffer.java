package util;

/**
 * Decides what kind of image a file really is from its first bytes. The file name and the Content-Type
 * the browser sends are chosen by the uploader, so neither is trusted.
 */
public final class ImageSniffer {
    private static final int[] PNG_SIGNATURE = {0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    private ImageSniffer() {
    }

    /** "jpg" for a JPEG, "png" for a PNG, or null for anything else. */
    public static String extensionFor(byte[] data) {
        if (data == null) {
            return null;
        }
        if (data.length >= 3
                && (data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8 && (data[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (data.length >= PNG_SIGNATURE.length) {
            for (int i = 0; i < PNG_SIGNATURE.length; i++) {
                if ((data[i] & 0xFF) != PNG_SIGNATURE[i]) {
                    return null;
                }
            }
            return "png";
        }
        return null;
    }

    public static String contentTypeFor(String extension) {
        return "png".equals(extension) ? "image/png" : "image/jpeg";
    }
}
