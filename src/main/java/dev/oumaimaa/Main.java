package dev.oumaimaa;

import dev.oumaimaa.commands.MailCommand;
import dev.oumaimaa.config.ConfigManager;
import dev.oumaimaa.database.MongoDBManager;
import dev.oumaimaa.listeners.PlayerConnectionListener;
import dev.oumaimaa.managers.MailboxManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * KawaiiMailbox - Professional offline mailbox system with MongoDB integration
 *
 * <p>This plugin provides a comprehensive mailbox system allowing players to send
 * messages and items to offline players, with persistent storage in MongoDB.</p>
 *
 * @author Oumaimaa
 * @version 1.0.0
 */
public final class Main extends JavaPlugin {

    private static Main instance;
    private MongoDBManager mongoDBManager;
    private ConfigManager configManager;
    private MailboxManager mailboxManager;

    /**
     * Retrieves the singleton instance of the plugin
     *
     * @return the plugin instance
     */
    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        long startTime = System.currentTimeMillis();
        getLogger().info("Initializing KawaiiMailbox...");

        if (!initializeConfiguration()) {
            getLogger().severe("Failed to initialize configuration. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!initializeMongoDB()) {
            getLogger().severe("Failed to connect to MongoDB. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        initializeManagers();
        registerCommands();
        registerListeners();

        long duration = System.currentTimeMillis() - startTime;
        getLogger().info("KawaiiMailbox enabled successfully in " + duration + "ms!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling KawaiiMailbox...");

        if (mongoDBManager != null) {
            mongoDBManager.close();
            getLogger().info("MongoDB connection closed.");
        }

        getLogger().info("KawaiiMailbox disabled successfully!");
    }

    /**
     * Initializes the configuration manager and loads all configuration files
     *
     * @return true if configuration was initialized successfully, false otherwise
     */
    private boolean initializeConfiguration() {
        try {
            configManager = new ConfigManager(this);
            configManager.loadConfigurations();
            getLogger().info("Configuration loaded successfully.");
            return true;
        } catch (Exception e) {
            getLogger().severe("Failed to load configuration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Initializes the MongoDB connection and verifies connectivity
     *
     * @return true if MongoDB connection was established successfully, false otherwise
     */
    private boolean initializeMongoDB() {
        try {
            mongoDBManager = new MongoDBManager(this);
            if (mongoDBManager.connect()) {
                getLogger().info("MongoDB connection established successfully.");
                return true;
            } else {
                getLogger().severe("Failed to establish MongoDB connection.");
                return false;
            }
        } catch (Exception e) {
            getLogger().severe("MongoDB initialization error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Initializes all manager classes required by the plugin
     */
    private void initializeManagers() {
        mailboxManager = new MailboxManager(this);
        getLogger().info("Managers initialized successfully.");
    }

    /**
     * Registers all plugin commands with their executors and tab completer
     */
    private void registerCommands() {
        MailCommand mailCommand = new MailCommand(this);
        Objects.requireNonNull(getCommand("mail")).setExecutor(mailCommand);
        Objects.requireNonNull(getCommand("mail")).setTabCompleter(mailCommand);
        getLogger().info("Commands registered successfully.");
    }

    /**
     * Registers all event listeners for the plugin
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getLogger().info("Listeners registered successfully.");
    }

    /**
     * Retrieves the MongoDB manager instance
     *
     * @return the MongoDB manager
     */
    public MongoDBManager getMongoDBManager() {
        return mongoDBManager;
    }

    /**
     * Retrieves the configuration manager instance
     *
     * @return the configuration manager
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Retrieves the mailbox manager instance
     *
     * @return the mailbox manager
     */
    public MailboxManager getMailboxManager() {
        return mailboxManager;
    }
}