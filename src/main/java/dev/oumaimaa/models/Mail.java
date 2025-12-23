package dev.oumaimaa.models;

import org.bson.Document;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a mail message in the mailbox system
 *
 * <p>This class encapsulates all data associated with a mail message including
 * sender, recipient, content, attachments, and status information.</p>
 *
 * @author oumaimaa
 * @version 1.0.0
 */
public class Mail {

    private final String id;
    private final UUID sender;
    private final String senderName;
    private final UUID recipient;
    private final String recipientName;
    private final String message;
    private final long timestamp;
    private boolean read;
    private boolean itemsClaimed;
    private final List<ItemStack> items;

    /**
     * Constructs a new mail message
     *
     * @param id the unique identifier
     * @param sender the sender's UUID
     * @param senderName the sender's name
     * @param recipient the recipient's UUID
     * @param recipientName the recipient's name
     * @param message the message content
     * @param timestamp the creation timestamp
     * @param read the read status
     * @param itemsClaimed the items claimed status
     * @param items the attached items
     */
    public Mail(String id, UUID sender, String senderName, UUID recipient, String recipientName,
                String message, long timestamp, boolean read, boolean itemsClaimed, List<ItemStack> items) {
        this.id = id;
        this.sender = sender;
        this.senderName = senderName;
        this.recipient = recipient;
        this.recipientName = recipientName;
        this.message = message;
        this.timestamp = timestamp;
        this.read = read;
        this.itemsClaimed = itemsClaimed;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
    }

    /**
     * Creates a Mail instance from a MongoDB document
     *
     * @param doc the MongoDB document
     * @return the Mail instance
     */
    @Contract("_ -> new")
    public static @NotNull Mail fromDocument(@NotNull Document doc) {
        String id = doc.getString("_id");
        UUID sender = UUID.fromString(doc.getString("sender"));
        String senderName = doc.getString("senderName");
        UUID recipient = UUID.fromString(doc.getString("recipient"));
        String recipientName = doc.getString("recipientName");
        String message = doc.getString("message");
        long timestamp = doc.getLong("timestamp");
        boolean read = doc.getBoolean("read", false);
        boolean itemsClaimed = doc.getBoolean("itemsClaimed", false);

        List<ItemStack> items = new ArrayList<>();
        List<String> itemsData = doc.getList("items", String.class);
        if (itemsData != null) {
            for (String itemData : itemsData) {
                try {
                    ItemStack item = ItemStackSerializer.deserialize(itemData);
                    if (item != null) {
                        items.add(item);
                    }
                } catch (Exception e) {
                    // Log but don't fail - skip corrupted items
                }
            }
        }

        return new Mail(id, sender, senderName, recipient, recipientName, message,
                timestamp, read, itemsClaimed, items);
    }

    /**
     * Converts this Mail instance to a MongoDB document
     *
     * @return the MongoDB document
     */
    public Document toDocument() {
        Document doc = new Document()
                .append("_id", id)
                .append("sender", sender.toString())
                .append("senderName", senderName)
                .append("recipient", recipient.toString())
                .append("recipientName", recipientName)
                .append("message", message)
                .append("timestamp", timestamp)
                .append("read", read)
                .append("itemsClaimed", itemsClaimed);

        if (!items.isEmpty()) {
            List<String> itemsData = new ArrayList<>();
            for (ItemStack item : items) {
                itemsData.add(ItemStackSerializer.serialize(item));
            }
            doc.append("items", itemsData);
        } else {
            doc.append("items", new ArrayList<>());
        }

        return doc;
    }

    /**
     * Retrieves the mail ID
     *
     * @return the mail ID
     */
    public String getId() {
        return id;
    }

    /**
     * Retrieves the sender's UUID
     *
     * @return the sender's UUID
     */
    public UUID getSender() {
        return sender;
    }

    /**
     * Retrieves the sender's name
     *
     * @return the sender's name
     */
    public String getSenderName() {
        return senderName;
    }

    /**
     * Retrieves the recipient's UUID
     *
     * @return the recipient's UUID
     */
    public UUID getRecipient() {
        return recipient;
    }

    /**
     * Retrieves the recipient's name
     *
     * @return the recipient's name
     */
    public String getRecipientName() {
        return recipientName;
    }

    /**
     * Retrieves the message content
     *
     * @return the message content
     */
    public String getMessage() {
        return message;
    }

    /**
     * Retrieves the timestamp
     *
     * @return the timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Checks if the mail has been read
     *
     * @return true if read, false otherwise
     */
    public boolean isRead() {
        return read;
    }

    /**
     * Sets the read status
     *
     * @param read the new read status
     */
    public void setRead(boolean read) {
        this.read = read;
    }

    /**
     * Checks if items have been claimed
     *
     * @return true if claimed, false otherwise
     */
    public boolean isItemsClaimed() {
        return itemsClaimed;
    }

    /**
     * Sets the items claimed status
     *
     * @param itemsClaimed the new claimed status
     */
    public void setItemsClaimed(boolean itemsClaimed) {
        this.itemsClaimed = itemsClaimed;
    }

    /**
     * Retrieves the attached items
     *
     * @return an unmodifiable list of items
     */
    public List<ItemStack> getItems() {
        return new ArrayList<>(items);
    }

    /**
     * Checks if the mail has attached items
     *
     * @return true if items are attached, false otherwise
     */
    public boolean hasItems() {
        return !items.isEmpty();
    }

    /**
     * Retrieves a preview of the message (truncated if necessary)
     *
     * @param maxLength the maximum length of the preview
     * @return the message preview
     */
    public String getMessagePreview(int maxLength) {
        if (message.length() <= maxLength) {
            return message;
        }
        return message.substring(0, maxLength) + "...";
    }
}