package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * MySQL connection settings and connection factory for DAOs.
 *
 * Credentials are never stored in the repository. Each setting is read from an environment variable
 * or, if that is unset, a JVM system property of the same name (for example -DSPARTANFINDS_DB_URL=...
 * in Tomcat's bin/setenv script; see Backend/tomcat/setenv.sh.example):
 *
 *   SPARTANFINDS_DB_URL       JDBC URL, e.g. jdbc:mysql://localhost:3306/your_database_name
 *   SPARTANFINDS_DB_USER      database user
 *   SPARTANFINDS_DB_PASSWORD  database password
 *
 * Callers must close the returned Connection (try-with-resources). Connections are opened per call;
 * a connection pool can replace this later without changing DAO code.
 */
public final class DatabaseConfig {
    static final String URL_KEY = "SPARTANFINDS_DB_URL";
    static final String USER_KEY = "SPARTANFINDS_DB_USER";
    static final String PASSWORD_KEY = "SPARTANFINDS_DB_PASSWORD";

    // The driver jar is bundled in the WAR (WEB-INF/lib). DriverManager's automatic driver discovery
    // can miss drivers loaded by a Tomcat webapp class loader ("No suitable driver found"), so load it
    // explicitly once.
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("MySQL JDBC driver (mysql-connector-j) is missing from the WAR");
        }
    }

    private DatabaseConfig() {
    }

    /**
     * Opens a new connection using the configured settings.
     *
     * @throws IllegalStateException if a required setting is missing (names the setting, never its value)
     * @throws SQLException if the database cannot be reached or rejects the credentials; callers should
     *                      log it server-side and show users a generic error, not this message
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                require(URL_KEY),
                require(USER_KEY),
                require(PASSWORD_KEY));
    }

    private static String require(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = System.getProperty(key);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Database setting " + key + " is not set (use an environment variable or JVM system property)");
        }
        return value;
    }
}
