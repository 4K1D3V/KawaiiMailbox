package dev.oumaimaa.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import dev.oumaimaa.Main;
import org.bson.Document;

import java.util.concurrent.TimeUnit;

/**
 * Manages MongoDB connections and provides access to collections
 *
 * <p>This class handles the lifecycle of MongoDB connections including
 * initialization, connection pooling, and graceful shutdown.</p>
 *
 * @author oumaimaa
 * @version 1.0.0
 */
public class MongoDBManager {

    private final Main plugin;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private boolean connected;

    /**
     * Constructs a new MongoDB manager
     *
     * @param plugin the main plugin instance
     */
    public MongoDBManager(Main plugin) {
        this.plugin = plugin;
        this.connected = false;
    }

    /**
     * Establishes connection to MongoDB using configuration settings
     *
     * @return true if connection was successful, false otherwise
     */
    public boolean connect() {
        try {
            String connectionString = plugin.getConfigManager().getDatabaseConnectionString();
            String databaseName = plugin.getConfigManager().getDatabaseName();

            if (connectionString == null || connectionString.isEmpty()) {
                plugin.getLogger().severe("MongoDB connection string is not configured!");
                return false;
            }

            if (databaseName == null || databaseName.isEmpty()) {
                plugin.getLogger().severe("MongoDB database name is not configured!");
                return false;
            }

            // Configure MongoDB client settings
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(connectionString))
                    .serverApi(ServerApi.builder()
                            .version(ServerApiVersion.V1)
                            .build())
                    .applyToConnectionPoolSettings(builder -> builder
                            .maxSize(50)
                            .minSize(5)
                            .maxWaitTime(30, TimeUnit.SECONDS)
                            .maxConnectionIdleTime(60, TimeUnit.SECONDS))
                    .applyToSocketSettings(builder -> builder
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(10, TimeUnit.SECONDS))
                    .build();

            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase(databaseName);

            // Test connection
            database.runCommand(new Document("ping", 1));

            connected = true;
            plugin.getLogger().info("Successfully connected to MongoDB: " + databaseName);

            // Initialize collections and indexes
            initializeCollections();

            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to connect to MongoDB: " + e.getMessage());
            e.printStackTrace();
            connected = false;
            return false;
        }
    }

    /**
     * Initializes required collections and creates necessary indexes
     */
    private void initializeCollections() {
        try {
            // Create mailbox collection if it doesn't exist
            boolean mailboxExists = database.listCollectionNames()
                    .into(new java.util.ArrayList<>())
                    .contains("mailbox");

            if (!mailboxExists) {
                database.createCollection("mailbox");
                plugin.getLogger().info("Created 'mailbox' collection");
            }

            // Create indexes for better query performance
            MongoCollection<Document> mailboxCollection = getMailboxCollection();

            // Index on recipient for faster inbox queries
            mailboxCollection.createIndex(new Document("recipient", 1));

            // Compound index for recipient and read status
            mailboxCollection.createIndex(new Document("recipient", 1)
                    .append("read", 1));

            // Index on timestamp for sorting
            mailboxCollection.createIndex(new Document("timestamp", -1));

            plugin.getLogger().info("MongoDB indexes created successfully");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize collections: " + e.getMessage());
        }
    }

    /**
     * Retrieves the mailbox collection
     *
     * @return the mailbox collection, or null if not connected
     */
    public MongoCollection<Document> getMailboxCollection() {
        if (!connected || database == null) {
            plugin.getLogger().warning("Attempted to access mailbox collection while not connected to MongoDB");
            return null;
        }
        return database.getCollection("mailbox");
    }

    /**
     * Checks if the MongoDB connection is active
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        if (!connected || mongoClient == null) {
            return false;
        }

        try {
            // Ping the database to verify connection
            database.runCommand(new Document("ping", 1));
            return true;
        } catch (Exception e) {
            connected = false;
            return false;
        }
    }

    /**
     * Attempts to reconnect to MongoDB if connection was lost
     *
     * @return true if reconnection was successful, false otherwise
     */
    public boolean reconnect() {
        plugin.getLogger().info("Attempting to reconnect to MongoDB...");
        close();
        return connect();
    }

    /**
     * Closes the MongoDB connection gracefully
     */
    public void close() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
                plugin.getLogger().info("MongoDB connection closed gracefully");
            } catch (Exception e) {
                plugin.getLogger().warning("Error closing MongoDB connection: " + e.getMessage());
            } finally {
                mongoClient = null;
                database = null;
                connected = false;
            }
        }
    }

    /**
     * Retrieves the MongoDB database instance
     *
     * @return the database instance, or null if not connected
     */
    public MongoDatabase getDatabase() {
        return database;
    }
}