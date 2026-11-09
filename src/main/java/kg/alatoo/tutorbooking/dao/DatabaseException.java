package kg.alatoo.tutorbooking.dao;

/**
 * Wraps the checked SQLException so callers don't need try/catch everywhere,
 * while keeping the original cause for debugging.
 */
public class DatabaseException extends RuntimeException {
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
