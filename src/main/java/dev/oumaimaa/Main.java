package dev.oumaimaa;

import dev.oumaimaa.command.MailCommand;
import dev.oumaimaa.config.MainConfig;
import dev.oumaimaa.config.MessagesConfig;
import dev.oumaimaa.listener.PlayerJoinListener;
import dev.oumaimaa.mongodb.MongoManager;
import dev.oumaimaa.session.SessionManager;
import net.kyori.adventure.key.Key;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.slimecraft.bedrock.annotation.plugin.Plugin;
import org.slimecraft.bedrock.event.EventNode;

/**
 * KawaiiMailbox - An advanced offline mailbox system with MongoDB integration.
 * Allows players to send and receive messages and items while offline.
 *
 * <p>This plugin provides:</p>
 * <ul>
 *   <li>Persistent mail storage using MongoDB</li>
 *   <li>Item attachment support with serialization</li>
 *   <li>Rich notifications with sounds, particles, and fireworks</li>
 *   <li>Intuitive GUI-based mail management</li>
 *   <li>Asynchronous database operations</li>
 * </ul>
 *
 * @author Oumaimaa
 * @version 1.0.0
 * @since 1.0.0
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
    private BukkitTask sessionCleanupTask;

    /**
     * Gets the plugin instance.
     *
     * @return the singleton plugin instance
     */
    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        final long startTime = System.currentTimeMillis();

        try {
            // Initialize event node
            pluginNode = new EventNode(Key.key("kawaiimailbox", "main"));
            pluginNode.attachTo(EventNode.global());

            // Load configurations
            loadConfigurations();

            // Initialize MongoDB
            if (!initializeMongoDB()) {
                getLogger().severe("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                getLogger().severe("  MongoDB initialization failed!");
                getLogger().severe("  Plugin will continue with limited functionality.");
                getLogger().severe("  Please verify your configuration in config.yml");
                getLogger().severe("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            }

            // Register components
            registerCommands();
            registerListeners();
            startSessionCleanup();

            final long loadTime = System.currentTimeMillis() - startTime;
            getLogger().info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            getLogger().info("  KawaiiMailbox v1.0.0 enabled successfully!");
            getLogger().info("  Load time: " + loadTime + "ms");
            getLogger().info("  MongoDB: " + (mongoManager != null && mongoManager.isConnected() ? "✓ Connected" : "✗ Disconnected"));
            getLogger().info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        } catch (Exception e) {
            getLogger().severe("Fatal error during plugin initialization!");
            getLogger().severe("Error: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            // Stop session cleanup task
            if (sessionCleanupTask != null && !sessionCleanupTask.isCancelled()) {
                sessionCleanupTask.cancel();
            }

            // Clear all sessions
            SessionManager.clearAll();

            // Close MongoDB connection
            if (mongoManager != null) {
                mongoManager.close();
            }

            // Event node will be garbage collected automatically
            // No need to manually detach as Bedrock handles cleanup

            getLogger().info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            getLogger().info("  KawaiiMailbox disabled successfully!");
            getLogger().info("  All resources have been cleaned up.");
            getLogger().info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        } catch (Exception e) {
            getLogger().severe("Error during plugin shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads all configuration files with error handling.
     */
    private void loadConfigurations() {
        try {
            mainConfig = MainConfig.load();
            messagesConfig = MessagesConfig.load();
            getLogger().info("✓ Configurations loaded successfully");
        } catch (Exception e) {
            getLogger().severe("Failed to load configurations!");
            throw new RuntimeException("Configuration loading failed", e);
        }
    }

    /**
     * Initializes MongoDB connection with comprehensive error handling.
     *
     * @return true if initialization was successful, false otherwise
     */
    private boolean initializeMongoDB() {
        try {
            mongoManager = new MongoManager(
                    mainConfig.getMongoUri(),
                    mainConfig.getDatabaseName()
            );

            if (mongoManager.isConnected()) {
                getLogger().info("✓ MongoDB connection established");
                return true;
            } else {
                getLogger().warning("✗ MongoDB connection unavailable");
                return false;
            }

        } catch (Exception e) {
            getLogger().severe("MongoDB initialization error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Registers all plugin commands with error handling.
     */
    private void registerCommands() {
        try {
            new MailCommand().register();
            getLogger().info("✓ Commands registered");
        } catch (Exception e) {
            getLogger().severe("Failed to register commands!");
            throw new RuntimeException("Command registration failed", e);
        }
    }

    /**
     * Registers all event listeners with error handling.
     */
    private void registerListeners() {
        try {
            pluginNode.addListener(PlayerJoinListener.create());
            getLogger().info("✓ Event listeners registered");
        } catch (Exception e) {
            getLogger().severe("Failed to register listeners!");
            throw new RuntimeException("Listener registration failed", e);
        }
    }

    /**
     * Starts the periodic session cleanup task.
     * This task removes expired mail composition sessions every 5 minutes.
     * Uses Bukkit's scheduler for reliable repeating execution.
     */
    private void startSessionCleanup() {
        // 5 minutes = 300 seconds = 6000 ticks
        final long delayTicks = 6000L;
        final long intervalTicks = 6000L;

        sessionCleanupTask = getServer().getScheduler().runTaskTimerAsynchronously(
                this,
                () -> {
                    try {
                        SessionManager.cleanupExpired();
                        getLogger().fine("Session cleanup completed");
                    } catch (Exception e) {
                        getLogger().warning("Error during session cleanup: " + e.getMessage());
                    }
                },
                delayTicks,
                intervalTicks
        );

        getLogger().info("✓ Session cleanup task started (runs every 5 minutes)");
    }

    /**
     * Gets the MongoDB manager instance.
     *
     * @return the MongoDB manager, may be null if initialization failed
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

    /**
     * Reloads all plugin configurations.
     * Note: This does not reload MongoDB connection settings.
     *
     * @return true if reload was successful
     */
    public boolean reload() {
        try {
            mainConfig = MainConfig.load();
            messagesConfig = MessagesConfig.load();
            getLogger().info("Configuration reloaded successfully");
            return true;
        } catch (Exception e) {
            getLogger().severe("Failed to reload configuration: " + e.getMessage());
            return false;
        }
    }
}