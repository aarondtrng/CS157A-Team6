package config;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Where uploaded item photos are stored: a folder outside the WAR, so redeploying the app never deletes
 * them. Set SPARTANFINDS_UPLOAD_DIR (environment variable or JVM system property) to choose the folder;
 * otherwise it is "spartanfinds-uploads" in the home directory of the user running Tomcat.
 */
public final class UploadConfig {
    static final String DIR_KEY = "SPARTANFINDS_UPLOAD_DIR";

    private UploadConfig() {
    }

    public static Path uploadDir() {
        String value = System.getenv(DIR_KEY);
        if (value == null || value.isBlank()) {
            value = System.getProperty(DIR_KEY);
        }
        if (value == null || value.isBlank()) {
            return Paths.get(System.getProperty("user.home"), "spartanfinds-uploads").toAbsolutePath().normalize();
        }
        return Paths.get(value).toAbsolutePath().normalize();
    }
}
