package dev.oumaimaa.service;

import dev.oumaimaa.Main;
import dev.oumaimaa.model.MailMessage;
import dev.oumaimaa.repository.AsyncMailRepository;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service layer for mail operations. This class contains business logic
 * and acts as a facade between controllers (commands/GUIs) and repositories.
 *
 * <p>Benefits of this pattern:</p>
 * <ul>
 *   <li>Single Responsibility: Each layer has one clear purpose</li>
 *   <li>Testability: Business logic can be unit tested independently</li>
 *   <li>Maintainability: Changes to business rules are isolated</li>
 *   <li>Reusability: Same service methods used by commands and GUIs</li>
 * </ul>
 *
 * @author Oumaimaa
 * @version 1.0.0
 */
public final class MailService {

    private final Main plugin;
    private final AsyncMailRepository repository;

    public MailService(Main plugin) {
        this.plugin = plugin;
        this.repository = new AsyncMailRepository(plugin);
    }

    /**
     * Sends a mail message to a recipient.
     * Validates all inputs and handles business rules.
     *
     * @param sender    the sender
     * @param recipient the recipient
     * @param message   the message content
     * @param items     optional items to attach
     * @return a result indicating success or failure with details
     */
    public CompletableFuture<MailResult> sendMail(
            Player sender,
            OfflinePlayer recipient,
            String message,
            List<ItemStack> items) {

        return CompletableFuture.supplyAsync(() -> {
            // Business rule: Cannot send to self
            if (sender.getUniqueId().equals(recipient.getUniqueId())) {
                return MailResult.failure("CANNOT_SEND_TO_SELF");
            }

            // Business rule: Recipient must have played before
            if (!recipient.hasPlayedBefore() && !recipient.isOnline()) {
                return MailResult.failure("RECIPIENT_NOT_FOUND");
            }

            // Business rule: Message cannot be empty
            if (message == null || message.trim().isEmpty()) {
                return MailResult.failure("MESSAGE_EMPTY");
            }

            // Business rule: Item limit
            if (items != null && items.size() > plugin.getMainConfig().getMaxItemsPerMessage()) {
                return MailResult.failure("TOO_MANY_ITEMS");
            }

            // Create and send mail
            MailMessage mail = new MailMessage(
                    recipient.getUniqueId(),
                    recipient.getName(),
                    sender.getUniqueId(),
                    sender.getName(),
                    message
            );

            if (items != null && !items.isEmpty()) {
                mail.setItems(items);
            }

            boolean saved = repository.getDelegate().saveMail(mail);

            return saved
                    ? MailResult.success("Mail sent successfully", mail)
                    : MailResult.failure("DATABASE_ERROR");
        });
    }

    /**
     * Retrieves a player's inbox with pagination support.
     *
     * @param player the player
     * @param page   the page number (0-indexed)
     * @return a future containing the inbox page
     */
    public CompletableFuture<InboxPage> getInbox(Player player, int page) {
        return repository.getMailByRecipientAsync(player.getUniqueId())
                .thenApply(allMail -> {
                    int messagesPerPage = plugin.getMainConfig().getMessagesPerPage();
                    int totalPages = (int) Math.ceil((double) allMail.size() / messagesPerPage);
                    int currentPage = Math.max(0, Math.min(page, totalPages - 1));

                    int startIndex = currentPage * messagesPerPage;
                    int endIndex = Math.min(startIndex + messagesPerPage, allMail.size());

                    List<MailMessage> pageMessages = allMail.subList(startIndex, endIndex);

                    return new InboxPage(pageMessages, currentPage, totalPages, allMail.size());
                });
    }

    /**
     * Gets the unread mail count for a player.
     *
     * @param playerUuid the player's UUID
     * @return a future containing the unread count
     */
    public CompletableFuture<Long> getUnreadCount(UUID playerUuid) {
        return repository.getUnreadCountAsync(playerUuid);
    }

    /**
     * Marks a message as read.
     *
     * @param messageId the message ID
     * @return a future indicating success
     */
    public CompletableFuture<Boolean> markAsRead(String messageId) {
        return repository.updateStatusAsync(messageId, MailMessage.MailStatus.READ);
    }

    /**
     * Claims items from a message and gives them to the player.
     * Handles inventory full scenario.
     *
     * @param player  the player claiming items
     * @param message the message with items
     * @return a future containing the claim result
     */
    public CompletableFuture<ClaimResult> claimItems(Player player, MailMessage message) {
        return CompletableFuture.supplyAsync(() -> {
            if (!message.hasItems()) {
                return new ClaimResult(false, "NO_ITEMS", 0, 0);
            }

            if (message.isItemsClaimed()) {
                return new ClaimResult(false, "ALREADY_CLAIMED", 0, 0);
            }

            int itemsGiven = 0;
            int itemsDropped = 0;

            for (ItemStack item : message.getItems()) {
                if (player.getInventory().firstEmpty() == -1) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                    itemsDropped++;
                } else {
                    player.getInventory().addItem(item);
                    itemsGiven++;
                }
            }

            boolean marked = repository.getDelegate().markItemsClaimed(message.getId());

            return new ClaimResult(marked, "SUCCESS", itemsGiven, itemsDropped);
        });
    }

    /**
     * Deletes all read messages for a player.
     *
     * @param playerUuid the player's UUID
     * @return a future containing the number of deleted messages
     */
    public CompletableFuture<Long> clearReadMessages(UUID playerUuid) {
        return repository.deleteReadMessagesAsync(playerUuid);
    }

    /**
     * Result of a mail operation.
     */
    public record MailResult(
            boolean success,
            String messageKey,
            MailMessage mail
    ) {
        public static MailResult success(String messageKey, MailMessage mail) {
            return new MailResult(true, messageKey, mail);
        }

        public static MailResult failure(String messageKey) {
            return new MailResult(false, messageKey, null);
        }
    }

    /**
     * Represents a page of messages in an inbox.
     */
    public record InboxPage(
            List<MailMessage> messages,
            int currentPage,
            int totalPages,
            int totalMessages
    ) {
        public boolean hasNextPage() {
            return currentPage < totalPages - 1;
        }

        public boolean hasPreviousPage() {
            return currentPage > 0;
        }
    }

    /**
     * Result of claiming items from a message.
     */
    public record ClaimResult(
            boolean success,
            String messageKey,
            int itemsGiven,
            int itemsDropped
    ) {
    }
}