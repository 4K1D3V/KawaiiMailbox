package dev.oumaimaa.config;

import org.slimecraft.bedrock.annotation.config.Configuration;
import org.slimecraft.bedrock.annotation.config.ConfigurationValue;
import org.slimecraft.bedrock.config.ConfigLoader;
import org.slimecraft.bedrock.config.FileExtension;

/**
 * Main configuration for KawaiiMailbox plugin.
 * Contains MongoDB settings and general plugin options.
 */
@Configuration("config")
public final class MainConfig {

    @ConfigurationValue("mongodb.uri")
    private final String mongoUri = "mongodb://localhost:27017";

    @ConfigurationValue("mongodb.database")
    private final String databaseName = "kawaiimailbox";

    @ConfigurationValue("mongodb.connection-timeout-ms")
    private final Integer connectionTimeout = 10000;

    @ConfigurationValue("mongodb.socket-timeout-ms")
    private final Integer socketTimeout = 10000;

    @ConfigurationValue("mailbox.messages-per-page")
    private final Integer messagesPerPage = 27;

    @ConfigurationValue("mailbox.max-items-per-message")
    private final Integer maxItemsPerMessage = 27;

    @ConfigurationValue("mailbox.auto-open-inbox-on-join")
    private final Boolean autoOpenInboxOnJoin = false;

    @ConfigurationValue("mailbox.auto-open-delay-ticks")
    private final Long autoOpenDelayTicks = 40L;

    @ConfigurationValue("mailbox.notification-sound")
    private final String notificationSound = "BLOCK_NOTE_BLOCK_PLING";

    @ConfigurationValue("mailbox.notification-sound-volume")
    private final Float notificationSoundVolume = 1.0f;

    @ConfigurationValue("mailbox.notification-sound-pitch")
    private final Float notificationSoundPitch = 2.0f;

    @ConfigurationValue("mailbox.enable-particles")
    private final Boolean enableParticles = true;

    @ConfigurationValue("mailbox.particle-type")
    private final String particleType = "TOTEM_OF_UNDYING";

    @ConfigurationValue("mailbox.particle-count")
    private final Integer particleCount = 30;

    @ConfigurationValue("mailbox.enable-fireworks")
    private final Boolean enableFireworks = true;

    @ConfigurationValue("gui.inbox.title")
    private final String inboxGuiTitle = "<gradient:#ff6b9d:#ffc3a0>✉ Mail Inbox</gradient>";

    @ConfigurationValue("gui.inbox.rows")
    private final Integer inboxGuiRows = 6;

    @ConfigurationValue("gui.message-details.title")
    private final String messageDetailsGuiTitle = "<gradient:#c471ed:#f64f59>✉ Message Details</gradient>";

    @ConfigurationValue("gui.add-items.title")
    private final String addItemsGuiTitle = "<gradient:#43e97b:#38f9d7>✉ Add Items to Mail</gradient>";

    @ConfigurationValue("gui.add-items.rows")
    private final Integer addItemsGuiRows = 6;

    private MainConfig() {
    }

    /**
     * Loads the main configuration.
     *
     * @return the loaded configuration
     */
    public static MainConfig load() {
        return ConfigLoader.load(MainConfig.class, FileExtension.YML);
    }

    public String getMongoUri() {
        return mongoUri;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public Integer getSocketTimeout() {
        return socketTimeout;
    }

    public Integer getMessagesPerPage() {
        return messagesPerPage;
    }

    public Integer getMaxItemsPerMessage() {
        return maxItemsPerMessage;
    }

    public Boolean getAutoOpenInboxOnJoin() {
        return autoOpenInboxOnJoin;
    }

    public Long getAutoOpenDelayTicks() {
        return autoOpenDelayTicks;
    }

    public String getNotificationSound() {
        return notificationSound;
    }

    public Float getNotificationSoundVolume() {
        return notificationSoundVolume;
    }

    public Float getNotificationSoundPitch() {
        return notificationSoundPitch;
    }

    public Boolean getEnableParticles() {
        return enableParticles;
    }

    public String getParticleType() {
        return particleType;
    }

    public int getParticleCount() {
        return particleCount;
    }

    public Boolean getEnableFireworks() {
        return enableFireworks;
    }

    public String getInboxGuiTitle() {
        return inboxGuiTitle;
    }

    public Integer getInboxGuiRows() {
        return inboxGuiRows;
    }

    public String getMessageDetailsGuiTitle() {
        return messageDetailsGuiTitle;
    }

    public String getAddItemsGuiTitle() {
        return addItemsGuiTitle;
    }

    public Integer getAddItemsGuiRows() {
        return addItemsGuiRows;
    }
}