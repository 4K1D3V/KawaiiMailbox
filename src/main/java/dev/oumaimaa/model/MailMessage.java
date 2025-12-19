package dev.oumaimaa.model;

import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a mail message in the mailbox system.
 * Contains message content, sender/recipient info, items, and status.
 */
public final class MailMessage {

    private String id;
    private UUID recipientUuid;
    private String recipientName;
    private UUID senderUuid;
    private String senderName;
    private String message;
    private List<ItemStack> items;
    private long timestamp;
    private MailStatus status;
    private boolean itemsClaimed;

    /**
     * Creates a new mail message.
     */
    public MailMessage() {
        this.id = UUID.randomUUID().toString();
        this.items = new ArrayList<>();
        this.timestamp = Instant.now().toEpochMilli();
        this.status = MailStatus.UNREAD;
        this.itemsClaimed = false;
    }

    /**
     * Creates a new mail message with specified parameters.
     *
     * @param recipientUuid the recipient's UUID
     * @param recipientName the recipient's name
     * @param senderUuid    the sender's UUID
     * @param senderName    the sender's name
     * @param message       the message content
     */
    public MailMessage(UUID recipientUuid, String recipientName, UUID senderUuid,
                       String senderName, String message) {
        this();
        this.recipientUuid = recipientUuid;
        this.recipientName = recipientName;
        this.senderUuid = senderUuid;
        this.senderName = senderName;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UUID getRecipientUuid() {
        return recipientUuid;
    }

    public void setRecipientUuid(UUID recipientUuid) {
        this.recipientUuid = recipientUuid;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public UUID getSenderUuid() {
        return senderUuid;
    }

    public void setSenderUuid(UUID senderUuid) {
        this.senderUuid = senderUuid;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ItemStack> getItems() {
        return items != null ? items : new ArrayList<>();
    }

    public void setItems(List<ItemStack> items) {
        this.items = items;
    }

    public void addItem(ItemStack item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public MailStatus getStatus() {
        return status;
    }

    public void setStatus(MailStatus status) {
        this.status = status;
    }

    public boolean isItemsClaimed() {
        return itemsClaimed;
    }

    public void setItemsClaimed(boolean itemsClaimed) {
        this.itemsClaimed = itemsClaimed;
    }

    /**
     * Checks if this message has items attached.
     *
     * @return true if items are attached, false otherwise
     */
    public boolean hasItems() {
        return items != null && !items.isEmpty();
    }

    /**
     * Gets a preview of the message (first 30 characters).
     *
     * @return the message preview
     */
    public String getPreview() {
        if (message == null || message.isEmpty()) {
            return "No message";
        }
        return message.length() > 30 ? message.substring(0, 30) + "..." : message;
    }

    /**
     * Marks this message as read.
     */
    public void markAsRead() {
        this.status = MailStatus.READ;
    }

    /**
     * Checks if this message is unread.
     *
     * @return true if unread, false otherwise
     */
    public boolean isUnread() {
        return status == MailStatus.UNREAD;
    }

    /**
     * Enum representing the status of a mail message.
     */
    public enum MailStatus {
        UNREAD,
        READ
    }
}