package dev.oumaimaa.mongodb;

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
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * Manages MongoDB connection and provides access to collections.
 * Handles connection lifecycle and ensures graceful degradation on failures.
 *
 * <p>This class implements {@link AutoCloseable} for proper resource management
 * and should be used with try-with-resources when appropriate.</p>
 */
public final class MongoManager implements AutoCloseable {

    private final String connectionUri;
    private final String databaseName;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private boolean connected;

    /**
     * Creates a new MongoDB manager.
     *
     * @param connectionUri the MongoDB connection URI
     * @param databaseName  the database name
     */
    public MongoManager(String connectionUri, String databaseName) {
        this.connectionUri = connectionUri;
        this.databaseName = databaseName;

        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.SEVERE);

        connect();
    }

    /**
     * Establishes connection to MongoDB.
     */
    private void connect() {
        try {
            CodecRegistry pojoCodecRegistry = fromProviders(
                    PojoCodecProvider.builder().automatic(true).build()
            );

            CodecRegistry codecRegistry = fromRegistries(
                    MongoClientSettings.getDefaultCodecRegistry(),
                    pojoCodecRegistry
            );

            ConnectionString connString = new ConnectionString(connectionUri);

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connString)
                    .codecRegistry(codecRegistry)
                    .serverApi(ServerApi.builder()
                            .version(ServerApiVersion.V1)
                            .build())
                    .applyToConnectionPoolSettings(builder -> {
                        builder.maxSize(50) // Max connections in pool
                                .minSize(5)  // Min connections to maintain
                                .maxWaitTime(10000, TimeUnit.MILLISECONDS)
                                .maxConnectionLifeTime(30, TimeUnit.MINUTES)
                                .maxConnectionIdleTime(10, TimeUnit.MINUTES);
                    })
                    .applyToSocketSettings(builder -> {
                        builder.connectTimeout(
                                Main.getInstance().getMainConfig().getConnectionTimeout(),
                                TimeUnit.MILLISECONDS
                        );
                        builder.readTimeout(
                                Main.getInstance().getMainConfig().getSocketTimeout(),
                                TimeUnit.MILLISECONDS
                        );
                    })
                    .retryWrites(true)  // Enable automatic retry for write operations
                    .retryReads(true)   // Enable automatic retry for read operations
                    .build();

            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase(databaseName);

            // Test connection
            database.runCommand(new Document("ping", 1));
            connected = true;

            Main.getInstance().getLogger().info("Successfully connected to MongoDB!");

        } catch (Exception e) {
            connected = false;
            Main.getInstance().getLogger().severe("Failed to connect to MongoDB: " + e.getMessage());
            Main.getInstance().getLogger().severe("Plugin will continue with limited functionality.");
        }
    }

    /**
     * Gets the mailbox collection.
     *
     * @return the mailbox collection, or null if not connected
     */
    public @Nullable MongoCollection<Document> getMailboxCollection() {
        if (!connected || database == null) {
            return null;
        }
        return database.getCollection("mailbox");
    }

    /**
     * Checks if MongoDB is connected and available.
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        if (!connected) {
            return false;
        }

        try {
            database.runCommand(new Document("ping", 1));
            return true;
        } catch (Exception e) {
            Main.getInstance().getLogger().warning("MongoDB connection check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Reconnects to MongoDB if disconnected.
     *
     * @return true if reconnection was successful, false otherwise
     */
    public boolean reconnect() {
        if (isConnected()) {
            return true;
        }

        Main.getInstance().getLogger().info("Attempting to reconnect to MongoDB...");
        close();
        connect();
        return isConnected();
    }

    /**
     * Closes the MongoDB connection and releases resources.
     * This method is idempotent and can be called multiple times safely.
     */
    @Override
    public void close() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
                Main.getInstance().getLogger().info("MongoDB connection closed.");
            } catch (Exception e) {
                Main.getInstance().getLogger().warning("Error closing MongoDB connection: " + e.getMessage());
            }
        }
        connected = false;
        mongoClient = null;
        database = null;
    }

    /**
     * Gets the MongoDB client.
     *
     * @return the MongoDB client
     */
    public MongoClient getMongoClient() {
        return mongoClient;
    }

    /**
     * Gets the database instance.
     *
     * @return the database instance
     */
    public MongoDatabase getDatabase() {
        return database;
    }
}