package dev.oumaimaa.config;

import dev.oumaimaa.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Manages all configuration files for the plugin
 *
 * <p>This class handles loading, reloading, and accessing configuration values
 * from config.yml, messages.yml, and gui.yml files.</p>
 *
 * @author oumaimaa
 * @version 1.0.0
 */
public class ConfigManager {

    private final Main plugin;
    private final MiniMessage miniMessage;

    private FileConfiguration config;
    private FileConfiguration messages;
    private FileConfiguration guiConfig;

    private final Map<String, Component> messageCache;

    /**
     * Constructs a new configuration manager
     *
     * @param plugin the main plugin instance
     */
    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.messageCache = new HashMap<>();
    }

    /**
     * Loads all configuration files and creates defaults if they don't exist
     */
    public void loadConfigurations() {
        // Load config.yml
        saveDefaultConfig("config.yml");
        config = loadConfig("config.yml");

        // Load messages.yml
        saveDefaultConfig("messages.yml");
        messages = loadConfig("messages.yml");

        // Load gui.yml
        saveDefaultConfig("gui.yml");
        guiConfig = loadConfig("gui.yml");

        // Cache messages for performance
        cacheMessages();

        plugin.getLogger().info("All configuration files loaded successfully");
    }

    /**
     * Saves the default configuration file if it doesn't exist
     *
     * @param fileName the name of the configuration file
     */
    private void saveDefaultConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try (InputStream in = plugin.getResource(fileName)) {
                if (in != null) {
                    Files.copy(in, file.toPath());
                    plugin.getLogger().info("Created default " + fileName);
                } else {
                    plugin.getLogger().warning("Could not find default " + fileName + " in resources");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save default " + fileName + ": " + e.getMessage());
            }
        }
    }

    /**
     * Loads a YAML configuration file
     *
     * @param fileName the name of the file to load
     * @return the loaded configuration
     */
    private @NotNull FileConfiguration loadConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Caches all messages from messages.yml for faster access
     */
    private void cacheMessages() {
        messageCache.clear();
        ConfigurationSection section = messages.getConfigurationSection("messages");
        if (section != null) {
            for (String key : section.getKeys(true)) {
                if (messages.isString("messages." + key)) {
                    String rawMessage = messages.getString("messages." + key);
                    messageCache.put(key, miniMessage.deserialize(Objects.requireNonNull(rawMessage)));
                }
            }
        }
    }

    /**
     * Retrieves a message component with optional placeholder replacements
     *
     * @param key the message key
     * @param placeholders the placeholders to replace (key-value pairs)
     * @return the formatted message component
     */
    public Component getMessage(String key, String... placeholders) {
        Component message = messageCache.get(key);
        if (message == null) {
            String rawMessage = messages.getString("messages." + key, "<red>Message not found: " + key);
            message = miniMessage.deserialize(rawMessage);
        }

        // Apply placeholders
        if (placeholders.length > 0 && placeholders.length % 2 == 0) {
            String serialized = miniMessage.serialize(message);
            for (int i = 0; i < placeholders.length; i += 2) {
                serialized = serialized.replace(placeholders[i], placeholders[i + 1]);
            }
            message = miniMessage.deserialize(serialized);
        }

        return message;
    }

    /**
     * Retrieves a raw string from the main configuration
     *
     * @param path the configuration path
     * @param def the default value
     * @return the configuration value
     */
    public String getString(String path, String def) {
        return config.getString(path, def);
    }

    /**
     * Retrieves an integer from the main configuration
     *
     * @param path the configuration path
     * @param def the default value
     * @return the configuration value
     */
    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }

    /**
     * Retrieves a boolean from the main configuration
     *
     * @param path the configuration path
     * @param def the default value
     * @return the configuration value
     */
    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    /**
     * Retrieves the MongoDB connection string
     *
     * @return the connection string
     */
    public String getDatabaseConnectionString() {
        return config.getString("database.connection-string");
    }

    /**
     * Retrieves the MongoDB database name
     *
     * @return the database name
     */
    public String getDatabaseName() {
        return config.getString("database.database-name", "kawaii_mailbox");
    }

    /**
     * Retrieves the maximum number of messages to display per inbox page
     *
     * @return the messages per page
     */
    public int getMessagesPerPage() {
        return config.getInt("inbox.messages-per-page", 27);
    }

    /**
     * Retrieves the delay before auto-opening inbox on join
     *
     * @return the delay in ticks
     */
    public long getInboxAutoOpenDelay() {
        return config.getLong("inbox.auto-open-delay-ticks", 40L);
    }

    /**
     * Checks if inbox auto-open is enabled
     *
     * @return true if enabled, false otherwise
     */
    public boolean isInboxAutoOpenEnabled() {
        return config.getBoolean("inbox.auto-open-on-join", true);
    }

    /**
     * Retrieves a configuration section from the GUI config
     *
     * @param path the configuration path
     * @return the configuration section
     */
    public ConfigurationSection getGuiSection(String path) {
        return guiConfig.getConfigurationSection(path);
    }

    /**
     * Retrieves the GUI configuration
     *
     * @return the GUI configuration
     */
    public FileConfiguration getGuiConfig() {
        return guiConfig;
    }

    /**
     * Retrieves the messages configuration
     *
     * @return the messages configuration
     */
    public FileConfiguration getMessagesConfig() {
        return messages;
    }

    /**
     * Retrieves the main configuration
     *
     * @return the main configuration
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Reloads all configuration files
     */
    public void reload() {
        config = loadConfig("config.yml");
        messages = loadConfig("messages.yml");
        guiConfig = loadConfig("gui.yml");
        cacheMessages();
        plugin.getLogger().info("Configuration reloaded successfully");
    }
}