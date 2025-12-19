package dev.oumaimaa.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import dev.oumaimaa.Main;
import dev.oumaimaa.model.MailMessage;
import dev.oumaimaa.util.ItemSerializer;
import org.bson.Document;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Repository for accessing and managing mail messages in MongoDB.
 * Handles all database operations for the mailbox system.
 */
public final class MailRepository {

    private final Main plugin;

    public MailRepository(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * Saves a mail message to the database.
     *
     * @param mail the mail message to save
     * @return true if successful, false otherwise
     */
    public boolean saveMail(MailMessage mail) {
        try {
            MongoCollection<Document> collection = getCollection();
            if (collection == null) {
                return false;
            }

            Document doc = mailToDocument(mail);
            collection.insertOne(doc);
            return true;

        } catch (Exception e) {
            plugin.getLogger().severe("Error saving mail: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets all mail messages for a specific recipient.
     *
     * @param recipientUuid the recipient's UUID
     * @return list of mail messages
     */
    public @NotNull List<MailMessage> getMailByRecipient(UUID recipientUuid) {
        try {
            MongoCollection<Document> collection = getCollection();
            if (collection == null) {
                return new ArrayList<>();
            }

            List<MailMessage> messages = new ArrayList<>();
            collection.find(Filters.eq("recipientUuid", recipientUuid.toString()))
                    .sort(Sorts.descending("timestamp"))
                    .forEach(doc -> messages.add(documentToMail(doc)));

            return messages;

        } catch (Exception e) {
            plugin.getLogger().severe("Error getting mail: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Gets unread mail count for a recipient.
     *
     * @param recipientUuid the recipient's UUID
     * @return count of unread messages
     */
    public long getUnreadCount(UUID recipientUuid) {
        try {
            MongoCollection<Document> collection = getCollection();
            if (collection == null) {
                return 0;
            }

            return collection.countDocuments(Filters.and(
                    Filters.eq("recipientUuid", recipientUuid.toString()),
                    Filters.eq("status", "UNREAD")
            ));

        } catch (Exception e) {
            plugin.getLogger().severe("Error counting unread mail: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Updates a mail message's status.
     *
     * @param messageId the message ID
     * @param status    the new status
     * @return true if successful, false otherwise
     */
    public boolean updateStatus(String messageId, MailMessage.MailStatus status) {
        try {
            MongoCollection<Document> collection = getCollection();
            if (collection == null) {
                return false;
            }

            collection.updateOne(
                    Filters.eq("_id", messageId),
                    Updates.set("status", status.name())
            );
            return true;

        } catch (Exception e) {
            plugin.getLogger().severe("Error updating mail status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Marks items as claimed for a message.
     *
     * @param messageId the message ID
     * @return true if successful, false otherwise
     */
    public boolean markItemsClaimed(String messageId) {
        try {
            MongoCollection<Document> collection = getCollection();
            if (collection == null) {
                return false;
            }

            collection.updateOne(
                    Filters.eq("_id", messageId),
                    Updates.combine(
                            Updates.set("itemsClaimed", true),
                            Updates.set("status", MailMessage.MailStatus.READ.name())
                    )
            );
            return true;

        } catch (Exception e) {
            plugin.getLogger().severe("Error marking items claimed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes read messages for a recipient.
     *
     * @param recipientUuid the recipient's UUID
     * @return count of deleted messages
     */
    public long deleteReadMessages(UUID recipientUuid) {
        try {
            MongoCollection<Document> collection = getCollection();
            if (collection == null) {
                return 0;
            }

            return collection.deleteMany(Filters.and(
                    Filters.eq("recipientUuid", recipientUuid.toString()),
                    Filters.eq("status", "READ")
            )).getDeletedCount();

        } catch (Exception e) {
            plugin.getLogger().severe("Error deleting read messages: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Gets a specific mail message by ID.
     *
     * @param messageId the message ID
     * @return the mail message, or null if not found
     */
    public @Nullable MailMessage getMailById(String messageId) {
        try {
            MongoCollection<Document> collection = getCollection();
            if (collection == null) {
                return null;
            }

            Document doc = collection.find(Filters.eq("_id", messageId)).first();
            return doc != null ? documentToMail(doc) : null;

        } catch (Exception e) {
            plugin.getLogger().severe("Error getting mail by ID: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gets the mailbox collection from MongoDB.
     *
     * @return the collection, or null if unavailable
     */
    private MongoCollection<Document> getCollection() {
        if (!plugin.getMongoManager().isConnected()) {
            plugin.getMongoManager().reconnect();
        }
        return plugin.getMongoManager().getMailboxCollection();
    }

    /**
     * Converts a MailMessage to a MongoDB Document.
     *
     * @param mail the mail message
     * @return the document
     */
    private Document mailToDocument(@NotNull MailMessage mail) {
        Document doc = new Document("_id", mail.getId())
                .append("recipientUuid", mail.getRecipientUuid().toString())
                .append("recipientName", mail.getRecipientName())
                .append("senderUuid", mail.getSenderUuid().toString())
                .append("senderName", mail.getSenderName())
                .append("message", mail.getMessage())
                .append("timestamp", mail.getTimestamp())
                .append("status", mail.getStatus().name())
                .append("itemsClaimed", mail.isItemsClaimed());

        if (mail.hasItems()) {
            List<String> serializedItems = mail.getItems().stream()
                    .map(ItemSerializer::serialize)
                    .collect(Collectors.toList());
            doc.append("items", serializedItems);
        }

        return doc;
    }

    /**
     * Converts a MongoDB Document to a MailMessage.
     *
     * @param doc the document
     * @return the mail message
     */
    private @NotNull MailMessage documentToMail(@NotNull Document doc) {
        MailMessage mail = new MailMessage();
        mail.setId(doc.getString("_id"));
        mail.setRecipientUuid(UUID.fromString(doc.getString("recipientUuid")));
        mail.setRecipientName(doc.getString("recipientName"));
        mail.setSenderUuid(UUID.fromString(doc.getString("senderUuid")));
        mail.setSenderName(doc.getString("senderName"));
        mail.setMessage(doc.getString("message"));
        mail.setTimestamp(doc.getLong("timestamp"));
        mail.setStatus(MailMessage.MailStatus.valueOf(doc.getString("status")));
        mail.setItemsClaimed(doc.getBoolean("itemsClaimed", false));

        @SuppressWarnings("unchecked")
        List<String> itemsData = (List<String>) doc.get("items");
        if (itemsData != null && !itemsData.isEmpty()) {
            List<ItemStack> items = itemsData.stream()
                    .map(ItemSerializer::deserialize)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            mail.setItems(items);
        }

        return mail;
    }
}