package dao;

/**
 * Unchecked wrapper for database failures (SQLException, missing connection settings). Servlets catch it,
 * log the cause server-side, and show users a generic error, so database details never reach a page.
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
