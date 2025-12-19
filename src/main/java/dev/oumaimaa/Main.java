package dev.oumaimaa;

import dev.oumaimaa.command.MailCommand;
import dev.oumaimaa.config.MainConfig;
import dev.oumaimaa.config.MessagesConfig;
import dev.oumaimaa.listener.PlayerJoinListener;
import dev.oumaimaa.mongodb.MongoManager;
import net.kyori.adventure.key.Key;
import org.bukkit.plugin.java.JavaPlugin;
import org.slimecraft.bedrock.annotation.plugin.Plugin;
import org.slimecraft.bedrock.event.EventNode;

/**
 * KawaiiMailbox - An advanced offline mailbox system with MongoDB integration.
 * Allows players to send and receive messages and items while offline.
 *
 * @author Oumaimaa
 * @version 1.0.0
 */
@Plugin(
        value = "KawaiiMailbox",
        version = "1.0.0",
        description = "Advanced offline mailbox system with MongoDB integration",
        apiVersion = "1.21"
)
public final class Main extends JavaPlugin {

    private static Main instance;
    private MongoManager mongoManager;
    private MainConfig mainConfig;
    private MessagesConfig messagesConfig;
    private EventNode pluginNode;

    /**
     * Gets the plugin instance.
     *
     * @return the plugin instance
     */
    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        pluginNode = new EventNode(Key.key("kawaiimailbox", "main"));
        pluginNode.attachTo(EventNode.global());

        loadConfigurations();

        if (!initializeMongoDB()) {
            getLogger().severe("Failed to initialize MongoDB! Plugin functionality will be limited.");
            getLogger().severe("Please check your MongoDB configuration in config.yml");
        }

        registerCommands();
        registerListeners();
        getLogger().info("KawaiiMailbox has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (mongoManager != null) {
            mongoManager.close();
        }

        getLogger().info("KawaiiMailbox has been disabled successfully!");
    }

    /**
     * Loads all configuration files.
     */
    private void loadConfigurations() {
        mainConfig = MainConfig.load();
        messagesConfig = MessagesConfig.load();
        getLogger().info("Configurations loaded successfully!");
    }

    /**
     * Initializes MongoDB connection.
     *
     * @return true if initialization was successful, false otherwise
     */
    private boolean initializeMongoDB() {
        try {
            mongoManager = new MongoManager(
                    mainConfig.getMongoUri(),
                    mainConfig.getDatabaseName()
            );
            return mongoManager.isConnected();
        } catch (Exception e) {
            getLogger().severe("MongoDB initialization error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Registers all plugin commands.
     */
    private void registerCommands() {
        new MailCommand().register();
        getLogger().info("Commands registered successfully!");
    }

    /**
     * Registers all event listeners.
     */
    private void registerListeners() {
        pluginNode.addListener(PlayerJoinListener.create());
        getLogger().info("Event listeners registered successfully!");
    }

    /**
     * Gets the MongoDB manager.
     *
     * @return the MongoDB manager
     */
    public MongoManager getMongoManager() {
        return mongoManager;
    }

    /**
     * Gets the main configuration.
     *
     * @return the main configuration
     */
    public MainConfig getMainConfig() {
        return mainConfig;
    }

    /**
     * Gets the messages configuration.
     *
     * @return the messages configuration
     */
    public MessagesConfig getMessagesConfig() {
        return messagesConfig;
    }

    /**
     * Gets the plugin event node.
     *
     * @return the event node
     */
    public EventNode getPluginNode() {
        return pluginNode;
    }
}