package dev.oumaimaa.repository;

import dev.oumaimaa.Main;
import dev.oumaimaa.model.MailMessage;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Asynchronous wrapper for {@link MailRepository} that performs all database
 * operations off the main thread to prevent server lag.
 *
 * <p>This is the recommended way to interact with the mail system as it ensures
 * that MongoDB operations never block the main game thread.</p>
 *
 * @author Oumaimaa
 * @version 1.0.0
 */
public final class AsyncMailRepository {

    private final MailRepository delegate;
    private final Main plugin;

    public AsyncMailRepository(Main plugin) {
        this.plugin = plugin;
        this.delegate = new MailRepository(plugin);
    }

    /**
     * Asynchronously saves a mail message to the database.
     *
     * @param mail the mail message to save
     * @return a CompletableFuture that completes with true if successful
     */
    @Contract("_ -> new")
    public @NotNull CompletableFuture<Boolean> saveMailAsync(MailMessage mail) {
        return CompletableFuture.supplyAsync(() -> delegate.saveMail(mail));
    }

    /**
     * Asynchronously saves a mail message with a callback.
     *
     * @param mail     the mail message to save
     * @param callback consumer called on main thread with result
     */
    public void saveMailAsync(MailMessage mail, Consumer<Boolean> callback) {
        CompletableFuture.supplyAsync(() -> delegate.saveMail(mail))
                .thenAcceptAsync(callback, runnable ->
                        Bukkit.getScheduler().runTask(plugin, runnable));
    }

    /**
     * Asynchronously retrieves all mail for a recipient.
     *
     * @param recipientUuid the recipient's UUID
     * @return a CompletableFuture with the list of messages
     */
    @Contract("_ -> new")
    public @NotNull CompletableFuture<List<MailMessage>> getMailByRecipientAsync(UUID recipientUuid) {
        return CompletableFuture.supplyAsync(() -> delegate.getMailByRecipient(recipientUuid));
    }

    /**
     * Asynchronously retrieves all mail for a recipient with a callback.
     *
     * @param recipientUuid the recipient's UUID
     * @param callback      consumer called on main thread with results
     */
    public void getMailByRecipientAsync(UUID recipientUuid, Consumer<List<MailMessage>> callback) {
        CompletableFuture.supplyAsync(() -> delegate.getMailByRecipient(recipientUuid))
                .thenAcceptAsync(callback, runnable ->
                        Bukkit.getScheduler().runTask(plugin, runnable));
    }

    /**
     * Asynchronously gets the unread mail count for a recipient.
     *
     * @param recipientUuid the recipient's UUID
     * @return a CompletableFuture with the unread count
     */
    @Contract("_ -> new")
    public @NotNull CompletableFuture<Long> getUnreadCountAsync(UUID recipientUuid) {
        return CompletableFuture.supplyAsync(() -> delegate.getUnreadCount(recipientUuid));
    }

    /**
     * Asynchronously gets the unread mail count with a callback.
     *
     * @param recipientUuid the recipient's UUID
     * @param callback      consumer called on main thread with count
     */
    public void getUnreadCountAsync(UUID recipientUuid, Consumer<Long> callback) {
        CompletableFuture.supplyAsync(() -> delegate.getUnreadCount(recipientUuid))
                .thenAcceptAsync(callback, runnable ->
                        Bukkit.getScheduler().runTask(plugin, runnable));
    }

    /**
     * Asynchronously updates a message's status.
     *
     * @param messageId the message ID
     * @param status    the new status
     * @return a CompletableFuture that completes with true if successful
     */
    @Contract("_, _ -> new")
    public @NotNull CompletableFuture<Boolean> updateStatusAsync(String messageId, MailMessage.MailStatus status) {
        return CompletableFuture.supplyAsync(() -> delegate.updateStatus(messageId, status));
    }

    /**
     * Asynchronously marks items as claimed.
     *
     * @param messageId the message ID
     * @return a CompletableFuture that completes with true if successful
     */
    @Contract("_ -> new")
    public @NotNull CompletableFuture<Boolean> markItemsClaimedAsync(String messageId) {
        return CompletableFuture.supplyAsync(() -> delegate.markItemsClaimed(messageId));
    }

    /**
     * Asynchronously deletes read messages for a recipient.
     *
     * @param recipientUuid the recipient's UUID
     * @return a CompletableFuture with the count of deleted messages
     */
    @Contract("_ -> new")
    public @NotNull CompletableFuture<Long> deleteReadMessagesAsync(UUID recipientUuid) {
        return CompletableFuture.supplyAsync(() -> delegate.deleteReadMessages(recipientUuid));
    }

    /**
     * Asynchronously deletes read messages with a callback.
     *
     * @param recipientUuid the recipient's UUID
     * @param callback      consumer called on main thread with deleted count
     */
    public void deleteReadMessagesAsync(UUID recipientUuid, Consumer<Long> callback) {
        CompletableFuture.supplyAsync(() -> delegate.deleteReadMessages(recipientUuid))
                .thenAcceptAsync(callback, runnable ->
                        Bukkit.getScheduler().runTask(plugin, runnable));
    }

    /**
     * Asynchronously gets a specific mail message by ID.
     *
     * @param messageId the message ID
     * @return a CompletableFuture with the message, or null if not found
     */
    @Contract("_ -> new")
    public @NotNull CompletableFuture<MailMessage> getMailByIdAsync(String messageId) {
        return CompletableFuture.supplyAsync(() -> delegate.getMailById(messageId));
    }

    /**
     * Gets the synchronous delegate repository.
     * Use only when you need blocking operations (rare).
     *
     * @return the synchronous repository
     */
    public MailRepository getDelegate() {
        return delegate;
    }
}