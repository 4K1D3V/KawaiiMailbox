package dev.oumaimaa.config;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slimecraft.bedrock.annotation.config.Configuration;
import org.slimecraft.bedrock.annotation.config.ConfigurationValue;
import org.slimecraft.bedrock.config.ConfigLoader;
import org.slimecraft.bedrock.config.FileExtension;

/**
 * Messages configuration for KawaiiMailbox plugin.
 * Contains all customizable messages and text displayed to players.
 */
@Configuration("messages")
public final class MessagesConfig {

    @ConfigurationValue("prefix")
    private final String prefix = "<gradient:#ff6b9d:#ffc3a0>✉ KawaiiMailbox</gradient> <gray>»</gray> ";

    @ConfigurationValue("mail.send.success")
    private final String mailSendSuccess = "<green>Mail sent successfully to <white>{player}</white>!</green>";

    @ConfigurationValue("mail.send.self")
    private final String mailSendSelf = "<red>You cannot send mail to yourself!</red>";

    @ConfigurationValue("mail.send.add-items-prompt")
    private final String mailSendAddItemsPrompt = "<yellow>Click here to add items to your mail</yellow>";

    @ConfigurationValue("mail.send.items-added")
    private final String mailSendItemsAdded = "<green>Items added successfully! Mail sent to <white>{player}</white>.</green>";

    @ConfigurationValue("mail.inbox.empty")
    private final String mailInboxEmpty = "<yellow>Your inbox is empty!</yellow>";

    @ConfigurationValue("mail.inbox.opened")
    private final String mailInboxOpened = "<green>Opening your inbox...</green>";

    @ConfigurationValue("mail.clear.success")
    private final String mailClearSuccess = "<green>Cleared <white>{count}</white> read messages!</green>";

    @ConfigurationValue("mail.clear.none")
    private final String mailClearNone = "<yellow>No read messages to clear!</yellow>";

    @ConfigurationValue("mail.notification.new-mail")
    private final String notificationNewMail = "<gradient:#43e97b:#38f9d7>✉ You have <white>{count}</white> new message(s)!</gradient>";

    @ConfigurationValue("mail.notification.click-to-open")
    private final String notificationClickToOpen = "<yellow>Click here to open your inbox</yellow>";

    @ConfigurationValue("mail.message.marked-read")
    private final String messageMarkedRead = "<green>Message marked as read!</green>";

    @ConfigurationValue("mail.message.items-claimed")
    private final String messageItemsClaimed = "<green>Items claimed successfully!</green>";

    @ConfigurationValue("mail.message.no-items")
    private final String messageNoItems = "<yellow>This message has no items!</yellow>";

    @ConfigurationValue("mail.message.already-claimed")
    private final String messageAlreadyClaimed = "<yellow>Items already claimed!</yellow>";

    @ConfigurationValue("error.player-not-found")
    private final String errorPlayerNotFound = "<red>Player <white>{player}</white> not found!</red>";

    @ConfigurationValue("error.no-permission")
    private final String errorNoPermission = "<red>You don't have permission to do that!</red>";

    @ConfigurationValue("error.invalid-usage")
    private final String errorInvalidUsage = "<red>Invalid usage! Use: <white>{usage}</white></red>";

    @ConfigurationValue("error.mongodb-unavailable")
    private final String errorMongodbUnavailable = "<red>Database is currently unavailable. Please try again later!</red>";

    @ConfigurationValue("error.internal-error")
    private final String errorInternalError = "<red>An internal error occurred. Please contact an administrator!</red>";

    @ConfigurationValue("gui.inbox.item.unread.name")
    private final String inboxItemUnreadName = "<gradient:#ff6b9d:#ffc3a0>✉ New Mail</gradient>";

    @ConfigurationValue("gui.inbox.item.read.name")
    private final String inboxItemReadName = "<gray>✉ Read Mail</gray>";

    @ConfigurationValue("gui.inbox.item.lore")
    private final String inboxItemLore = "<gray>From:</gray> <white>{sender}|<gray>Preview:</gray> <white>{preview}|<gray>Time:</gray> <white>{time}|<gray>Status:</gray> {status}||<yellow>Click to view details</yellow>";

    @ConfigurationValue("gui.inbox.pagination.next.name")
    private final String paginationNextName = "<green>→ Next Page</green>";

    @ConfigurationValue("gui.inbox.pagination.previous.name")
    private final String paginationPreviousName = "<green>← Previous Page</green>";

    @ConfigurationValue("gui.inbox.pagination.info.name")
    private final String paginationInfoName = "<gradient:#43e97b:#38f9d7>Page {current}/{total}</gradient>";

    @ConfigurationValue("gui.message-details.mark-read.name")
    private final String messageDetailsMarkReadName = "<green>✓ Mark as Read</green>";

    @ConfigurationValue("gui.message-details.mark-read.lore")
    private final String messageDetailsMarkReadLore = "<gray>Click to mark this message as read</gray>";

    @ConfigurationValue("gui.message-details.claim-items.name")
    private final String messageDetailsClaimItemsName = "<gradient:#43e97b:#38f9d7>✦ Claim Items</gradient>";

    @ConfigurationValue("gui.message-details.claim-items.lore")
    private final String messageDetailsClaimItemsLore = "<gray>Click to claim attached items</gray>";

    @ConfigurationValue("gui.message-details.back.name")
    private final String messageDetailsBackName = "<yellow>← Back to Inbox</yellow>";

    @ConfigurationValue("gui.message-details.message-display.name")
    private final String messageDisplayName = "<gradient:#c471ed:#f64f59>✉ Message</gradient>";

    @ConfigurationValue("gui.message-details.message-display.lore")
    private final String messageDisplayLore = "<white>{message}</white>||<gray>From:</gray> <white>{sender}</white>|<gray>Sent:</gray> <white>{time}</white>";

    @ConfigurationValue("gui.add-items.confirm.name")
    private final String addItemsConfirmName = "<green>✓ Confirm & Send</green>";

    @ConfigurationValue("gui.add-items.confirm.lore")
    private final String addItemsConfirmLore = "<gray>Click to send mail with added items</gray>";

    @ConfigurationValue("gui.add-items.cancel.name")
    private final String addItemsCancelName = "<red>✗ Cancel</red>";

    @ConfigurationValue("gui.add-items.cancel.lore")
    private final String addItemsCancelLore = "<gray>Click to cancel and return</gray>";

    @ConfigurationValue("gui.add-items.info.name")
    private final String addItemsInfoName = "<gradient:#43e97b:#38f9d7>ℹ Place Items</gradient>";

    @ConfigurationValue("gui.add-items.info.lore")
    private final String addItemsInfoLore = "<gray>Place items in the slots below|to attach them to your mail</gray>";

    private MessagesConfig() {
    }

    /**
     * Loads the messages configuration.
     *
     * @return the loaded configuration
     */
    public static MessagesConfig load() {
        return ConfigLoader.load(MessagesConfig.class, FileExtension.YML);
    }

    public String getPrefix() {
        return prefix;
    }

    @Contract(pure = true)
    public @NotNull String getMailSendSuccess() {
        return prefix + mailSendSuccess;
    }

    @Contract(pure = true)
    public @NotNull String getMailSendSelf() {
        return prefix + mailSendSelf;
    }

    @Contract(pure = true)
    public @NotNull String getMailSendAddItemsPrompt() {
        return prefix + mailSendAddItemsPrompt;
    }

    @Contract(pure = true)
    public @NotNull String getMailSendItemsAdded() {
        return prefix + mailSendItemsAdded;
    }

    @Contract(pure = true)
    public @NotNull String getMailInboxEmpty() {
        return prefix + mailInboxEmpty;
    }

    @Contract(pure = true)
    public @NotNull String getMailInboxOpened() {
        return prefix + mailInboxOpened;
    }

    @Contract(pure = true)
    public @NotNull String getMailClearSuccess() {
        return prefix + mailClearSuccess;
    }

    @Contract(pure = true)
    public @NotNull String getMailClearNone() {
        return prefix + mailClearNone;
    }

    public String getNotificationNewMail() {
        return notificationNewMail;
    }

    public String getNotificationClickToOpen() {
        return notificationClickToOpen;
    }

    @Contract(pure = true)
    public @NotNull String getMessageMarkedRead() {
        return prefix + messageMarkedRead;
    }

    @Contract(pure = true)
    public @NotNull String getMessageItemsClaimed() {
        return prefix + messageItemsClaimed;
    }

    @Contract(pure = true)
    public @NotNull String getMessageNoItems() {
        return prefix + messageNoItems;
    }

    @Contract(pure = true)
    public @NotNull String getMessageAlreadyClaimed() {
        return prefix + messageAlreadyClaimed;
    }

    @Contract(pure = true)
    public @NotNull String getErrorPlayerNotFound() {
        return prefix + errorPlayerNotFound;
    }

    @Contract(pure = true)
    public @NotNull String getErrorNoPermission() {
        return prefix + errorNoPermission;
    }

    @Contract(pure = true)
    public @NotNull String getErrorInvalidUsage() {
        return prefix + errorInvalidUsage;
    }

    @Contract(pure = true)
    public @NotNull String getErrorMongodbUnavailable() {
        return prefix + errorMongodbUnavailable;
    }

    @Contract(pure = true)
    public @NotNull String getErrorInternalError() {
        return prefix + errorInternalError;
    }

    public String getInboxItemUnreadName() {
        return inboxItemUnreadName;
    }

    public String getInboxItemReadName() {
        return inboxItemReadName;
    }

    public String getInboxItemLore() {
        return inboxItemLore;
    }

    public String getPaginationNextName() {
        return paginationNextName;
    }

    public String getPaginationPreviousName() {
        return paginationPreviousName;
    }

    public String getPaginationInfoName() {
        return paginationInfoName;
    }

    public String getMessageDetailsMarkReadName() {
        return messageDetailsMarkReadName;
    }

    public String getMessageDetailsMarkReadLore() {
        return messageDetailsMarkReadLore;
    }

    public String getMessageDetailsClaimItemsName() {
        return messageDetailsClaimItemsName;
    }

    public String getMessageDetailsClaimItemsLore() {
        return messageDetailsClaimItemsLore;
    }

    public String getMessageDetailsBackName() {
        return messageDetailsBackName;
    }

    public String getMessageDisplayName() {
        return messageDisplayName;
    }

    public String getMessageDisplayLore() {
        return messageDisplayLore;
    }

    public String getAddItemsConfirmName() {
        return addItemsConfirmName;
    }

    public String getAddItemsConfirmLore() {
        return addItemsConfirmLore;
    }

    public String getAddItemsCancelName() {
        return addItemsCancelName;
    }

    public String getAddItemsCancelLore() {
        return addItemsCancelLore;
    }

    public String getAddItemsInfoName() {
        return addItemsInfoName;
    }

    public String getAddItemsInfoLore() {
        return addItemsInfoLore;
    }
}