package dev.oumaimaa.exception;

/**
 * Custom exception hierarchy for mail system errors.
 * Provides type-safe error handling and clear error categorization.
 *
 * @author Oumaimaa
 * @version 1.0.0
 */
public sealed class MailException extends Exception
        permits MailException.DatabaseException,
        MailException.ValidationException,
        MailException.PermissionException {

    private final ErrorCode errorCode;

    protected MailException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    protected MailException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Error codes for different failure scenarios.
     */
    public enum ErrorCode {
        // Database errors
        DATABASE_ERROR("error.database-error"),
        DATABASE_UNAVAILABLE("error.mongodb-unavailable"),
        CONNECTION_FAILED("error.connection-failed"),

        // Validation errors
        INVALID_RECIPIENT("error.invalid-recipient"),
        MESSAGE_TOO_LONG("error.message-too-long"),
        MESSAGE_EMPTY("error.message-empty"),
        TOO_MANY_ITEMS("error.too-many-items"),
        CANNOT_SEND_TO_SELF("error.cannot-send-to-self"),

        // Permission errors
        NO_PERMISSION("error.no-permission"),

        // Item claim errors
        NO_ITEMS("error.no-items"),
        ITEMS_ALREADY_CLAIMED("error.items-already-claimed"),

        // General errors
        INTERNAL_ERROR("error.internal-error"),
        PLAYER_NOT_FOUND("error.player-not-found");

        private final String messageKey;

        ErrorCode(String messageKey) {
            this.messageKey = messageKey;
        }

        public String getMessageKey() {
            return messageKey;
        }
    }

    /**
     * Database-related errors.
     */
    public static final class DatabaseException extends MailException {
        public DatabaseException(String message) {
            super(message, ErrorCode.DATABASE_ERROR);
        }

        public DatabaseException(String message, Throwable cause) {
            super(message, cause, ErrorCode.DATABASE_ERROR);
        }
    }

    /**
     * Input validation errors.
     */
    public static final class ValidationException extends MailException {
        public ValidationException(String message, ErrorCode errorCode) {
            super(message, errorCode);
        }
    }

    /**
     * Permission-related errors.
     */
    public static final class PermissionException extends MailException {
        public PermissionException(String message) {
            super(message, ErrorCode.NO_PERMISSION);
        }
    }
}