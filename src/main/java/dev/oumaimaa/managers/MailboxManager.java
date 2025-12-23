package dev.oumaimaa.managers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import dev.oumaimaa.Main;
import dev.oumaimaa.models.Mail;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Manages all mailbox operations including sending, retrieving, and updating mail
 *
 * <p>This class provides asynchronous methods for all mail-related operations
 * to prevent blocking the main server thread during database operations.</p>
 *
 * @author oumaimaa
 * @version 1.0.0
 */
public class MailboxManager {

    private final Main plugin;

    /**
     * Constructs a new mailbox manager
     *
     * @param plugin the main plugin instance
     */
    public MailboxManager(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * Sends a new mail message asynchronously
     *
     * @param sender the sender's UUID
     * @param senderName the sender's name
     * @param recipient the recipient's UUID
     * @param recipientName the recipient's name
     * @param message the message content
     * @param items the attached items
     * @return a CompletableFuture containing the created Mail, or null if failed
     */
    public CompletableFuture<Mail> sendMail(UUID sender, String senderName, UUID recipient,
                                            String recipientName, String message, List<ItemStack> items) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!plugin.getMongoDBManager().isConnected()) {
                    plugin.getLogger().warning("Cannot send mail - MongoDB not connected");
                    return null;
                }

                MongoCollection<Document> collection = plugin.getMongoDBManager().getMailboxCollection();
                if (collection == null) {
                    return null;
                }

                String id = UUID.randomUUID().toString();
                long timestamp = System.currentTimeMillis();

                Mail mail = new Mail(id, sender, senderName, recipient, recipientName,
                        message, timestamp, false, false, items);

                collection.insertOne(mail.toDocument());
                plugin.getLogger().info("Mail sent from " + senderName + " to " + recipientName);

                return mail;
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to send mail: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        });
    }

    /**
     * Retrieves all mail for a specific recipient asynchronously
     *
     * @param recipient the recipient's UUID
     * @param page the page number (0-indexed)
     * @param pageSize the number of messages per page
     * @return a CompletableFuture containing the list of Mail
     */
    public CompletableFuture<List<Mail>> getInbox(UUID recipient, int page, int pageSize) {
        return CompletableFuture.supplyAsync(() -> {
            List<Mail> mailList = new ArrayList<>();
            try {
                if (!plugin.getMongoDBManager().isConnected()) {
                    plugin.getLogger().warning("Cannot retrieve inbox - MongoDB not connected");
                    return mailList;
                }

                MongoCollection<Document> collection = plugin.getMongoDBManager().getMailboxCollection();
                if (collection == null) {
                    return mailList;
                }

                Bson filter = Filters.eq("recipient", recipient.toString());
                int skip = page * pageSize;

                collection.find(filter)
                        .sort(Sorts.descending("timestamp"))
                        .skip(skip)
                        .limit(pageSize)
                        .forEach(doc -> mailList.add(Mail.fromDocument(doc)));

            } catch (Exception e) {
                plugin.getLogger().severe("Failed to retrieve inbox: " + e.getMessage());
                e.printStackTrace();
            }
            return mailList;
        });
    }

    /**
     * Counts unread mail for a specific recipient asynchronously
     *
     * @param recipient the recipient's UUID
     * @return a CompletableFuture containing the count of unread messages
     */
    public CompletableFuture<Long> countUnreadMail(UUID recipient) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!plugin.getMongoDBManager().isConnected()) {
                    return 0L;
                }

                MongoCollection<Document> collection = plugin.getMongoDBManager().getMailboxCollection();
                if (collection == null) {
                    return 0L;
                }

                Bson filter = Filters.and(
                        Filters.eq("recipient", recipient.toString()),
                        Filters.eq("read", false)
                );

                return collection.countDocuments(filter);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to count unread mail: " + e.getMessage());
                e.printStackTrace();
                return 0L;
            }
        });
    }

    /**
     * Retrieves a specific mail by ID asynchronously
     *
     * @param mailId the mail ID
     * @return a CompletableFuture containing the Mail, or null if not found
     */
    public CompletableFuture<Mail> getMailById(String mailId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!plugin.getMongoDBManager().isConnected()) {
                    return null;
                }

                MongoCollection<Document> collection = plugin.getMongoDBManager().getMailboxCollection();
                if (collection == null) {
                    return null;
                }

                Document doc = collection.find(Filters.eq("_id", mailId)).first();
                return doc != null ? Mail.fromDocument(doc) : null;
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to retrieve mail: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        });
    }

    /**
     * Marks a mail as read asynchronously
     *
     * @param mailId the mail ID
     * @return a CompletableFuture containing true if successful, false otherwise
     */
    public CompletableFuture<Boolean> markAsRead(String mailId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!plugin.getMongoDBManager().isConnected()) {
                    return false;
                }

                MongoCollection<Document> collection = plugin.getMongoDBManager().getMailboxCollection();
                if (collection == null) {
                    return false;
                }

                Bson filter = Filters.eq("_id", mailId);
                Bson update = Updates.set("read", true);

                collection.updateOne(filter, update);
                return true;
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to mark mail as read: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }

    /**
     * Marks items as claimed for a specific mail asynchronously
     *
     * @param mailId the mail ID
     * @return a CompletableFuture containing true if successful, false otherwise
     */
    public CompletableFuture<Boolean> markItemsClaimed(String mailId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!plugin.getMongoDBManager().isConnected()) {
                    return false;
                }

                MongoCollection<Document> collection = plugin.getMongoDBManager().getMailboxCollection();
                if (collection == null) {
                    return false;
                }

                Bson filter = Filters.eq("_id", mailId);
                Bson update = Updates.combine(
                        Updates.set("itemsClaimed", true),
                        Updates.set("read", true)
                );

                collection.updateOne(filter, update);
                return true;
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to mark items as claimed: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }

    /**
     * Deletes read mail for a specific recipient asynchronously
     *
     * @param recipient the recipient's UUID
     * @return a CompletableFuture containing the number of deleted messages
     */
    public CompletableFuture<Long> deleteReadMail(UUID recipient) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!plugin.getMongoDBManager().isConnected()) {
                    return 0L;
                }

                MongoCollection<Document> collection = plugin.getMongoDBManager().getMailboxCollection();
                if (collection == null) {
                    return 0L;
                }

                Bson filter = Filters.and(
                        Filters.eq("recipient", recipient.toString()),
                        Filters.eq("read", true)
                );

                return collection.deleteMany(filter).getDeletedCount();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to delete read mail: " + e.getMessage());
                e.printStackTrace();
                return 0L;
            }
        });
    }

    /**
     * Counts total mail for a specific recipient asynchronously
     *
     * @param recipient the recipient's UUID
     * @return a CompletableFuture containing the total message count
     */
    public CompletableFuture<Long> countTotalMail(UUID recipient) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!plugin.getMongoDBManager().isConnected()) {
                    return 0L;
                }

                MongoCollection<Document> collection = plugin.getMongoDBManager().getMailboxCollection();
                if (collection == null) {
                    return 0L;
                }

                return collection.countDocuments(Filters.eq("recipient", recipient.toString()));
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to count total mail: " + e.getMessage());
                e.printStackTrace();
                return 0L;
            }
        });
    }

    /**
     * Retrieves statistics for a player asynchronously
     *
     * @param playerUUID the player's UUID
     * @return a CompletableFuture containing a Document with statistics
     */
    public CompletableFuture<Document> getPlayerStats(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!plugin.getMongoDBManager().isConnected()) {
                    return new Document();
                }

                MongoCollection<Document> collection = plugin.getMongoDBManager().getMailboxCollection();
                if (collection == null) {
                    return new Document();
                }

                long totalReceived = collection.countDocuments(
                        Filters.eq("recipient", playerUUID.toString()));
                long totalSent = collection.countDocuments(
                        Filters.eq("sender", playerUUID.toString()));
                long unread = collection.countDocuments(
                        Filters.and(
                                Filters.eq("recipient", playerUUID.toString()),
                                Filters.eq("read", false)
                        ));

                return new Document()
                        .append("totalReceived", totalReceived)
                        .append("totalSent", totalSent)
                        .append("unread", unread);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to retrieve player stats: " + e.getMessage());
                e.printStackTrace();
                return new Document();
            }
        });
    }
}