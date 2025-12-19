package dev.oumaimaa.config.model;

/**
 * Immutable configuration models using Java 21 records.
 * These provide type-safety, immutability, and pattern matching support.
 */
public final class ConfigModel {

    private ConfigModel() {
    }

    /**
     * MongoDB connection configuration.
     *
     * @param uri               the MongoDB connection URI
     * @param database          the database name
     * @param connectionTimeout connection timeout in milliseconds
     * @param socketTimeout     socket timeout in milliseconds
     */
    public record MongoConfig(
            String uri,
            String database,
            int connectionTimeout,
            int socketTimeout
    ) {
        public MongoConfig {
            if (uri == null || uri.isBlank()) {
                throw new IllegalArgumentException("MongoDB URI cannot be null or blank");
            }
            if (database == null || database.isBlank()) {
                throw new IllegalArgumentException("Database name cannot be null or blank");
            }
            if (connectionTimeout <= 0) {
                throw new IllegalArgumentException("Connection timeout must be positive");
            }
            if (socketTimeout <= 0) {
                throw new IllegalArgumentException("Socket timeout must be positive");
            }
        }
    }

    /**
     * Mailbox feature configuration.
     *
     * @param messagesPerPage     messages displayed per page
     * @param maxItemsPerMessage  maximum items attachable to a message
     * @param autoOpenInboxOnJoin whether to auto-open inbox on join
     * @param autoOpenDelayTicks  delay before auto-opening inbox
     */
    public record MailboxConfig(
            int messagesPerPage,
            int maxItemsPerMessage,
            boolean autoOpenInboxOnJoin,
            long autoOpenDelayTicks
    ) {
        public MailboxConfig {
            if (messagesPerPage <= 0 || messagesPerPage > 54) {
                throw new IllegalArgumentException("Messages per page must be between 1 and 54");
            }
            if (maxItemsPerMessage <= 0 || maxItemsPerMessage > 27) {
                throw new IllegalArgumentException("Max items per message must be between 1 and 27");
            }
            if (autoOpenDelayTicks < 0) {
                throw new IllegalArgumentException("Auto-open delay cannot be negative");
            }
        }
    }

    /**
     * Notification settings configuration.
     *
     * @param sound           the sound effect name
     * @param volume          sound volume (0.0 to 1.0)
     * @param pitch           sound pitch (0.5 to 2.0)
     * @param enableParticles whether to show particles
     * @param particleType    the particle type name
     * @param particleCount   number of particles to spawn
     * @param enableFireworks whether to show fireworks
     */
    public record NotificationConfig(
            String sound,
            float volume,
            float pitch,
            boolean enableParticles,
            String particleType,
            int particleCount,
            boolean enableFireworks
    ) {
        public NotificationConfig {
            if (sound == null || sound.isBlank()) {
                throw new IllegalArgumentException("Sound name cannot be null or blank");
            }
            if (volume < 0.0f || volume > 1.0f) {
                throw new IllegalArgumentException("Volume must be between 0.0 and 1.0");
            }
            if (pitch < 0.5f || pitch > 2.0f) {
                throw new IllegalArgumentException("Pitch must be between 0.5 and 2.0");
            }
            if (particleCount < 0) {
                throw new IllegalArgumentException("Particle count cannot be negative");
            }
        }
    }

    /**
     * GUI configuration.
     *
     * @param inboxTitle          the inbox GUI title
     * @param inboxRows           number of rows in inbox GUI
     * @param messageDetailsTitle the message details GUI title
     * @param addItemsTitle       the add items GUI title
     * @param addItemsRows        number of rows in add items GUI
     */
    public record GuiConfig(
            String inboxTitle,
            int inboxRows,
            String messageDetailsTitle,
            String addItemsTitle,
            int addItemsRows
    ) {
        public GuiConfig {
            if (inboxTitle == null || inboxTitle.isBlank()) {
                throw new IllegalArgumentException("Inbox title cannot be null or blank");
            }
            if (inboxRows <= 0 || inboxRows > 6) {
                throw new IllegalArgumentException("Inbox rows must be between 1 and 6");
            }
            if (messageDetailsTitle == null || messageDetailsTitle.isBlank()) {
                throw new IllegalArgumentException("Message details title cannot be null or blank");
            }
            if (addItemsTitle == null || addItemsTitle.isBlank()) {
                throw new IllegalArgumentException("Add items title cannot be null or blank");
            }
            if (addItemsRows <= 0 || addItemsRows > 6) {
                throw new IllegalArgumentException("Add items rows must be between 1 and 6");
            }
        }
    }
}