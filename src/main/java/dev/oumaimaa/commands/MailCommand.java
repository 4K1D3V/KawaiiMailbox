package dev.oumaimaa.commands;

import dev.oumaimaa.Main;
import dev.oumaimaa.gui.AddItemsGUI;
import dev.oumaimaa.gui.InboxGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles all /mail command execution and tab completion
 *
 * <p>This class processes mail commands including send, inbox, and clear operations
 * with proper permission checks and error handling.</p>
 *
 * @author oumaimaa
 * @version 1.0.0
 */
public class MailCommand implements TabExecutor {

    private final Main plugin;
    private final Map<UUID, PendingMail> pendingMails;

    /**
     * Constructs a new mail command handler
     *
     * @param plugin the main plugin instance
     */
    public MailCommand(Main plugin) {
        this.plugin = plugin;
        this.pendingMails = new HashMap<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("only-players"));
            return true;
        }

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "send" -> handleSend(player, args);
            case "inbox" -> handleInbox(player);
            case "clear" -> handleClear(player);
            default -> sendUsage(player);
        }

        return true;
    }

    /**
     * Handles the /mail send command
     *
     * @param sender the command sender
     * @param args the command arguments
     */
    private void handleSend(Player sender, String @NotNull [] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.getConfigManager().getMessage("usage-send"));
            return;
        }

        String targetName = args[1];
        String message = String.join(" ", List.of(args).subList(2, args.length));

        // Validate message length
        int maxLength = plugin.getConfigManager().getInt("mail.max-message-length", 500);
        if (message.length() > maxLength) {
            sender.sendMessage(plugin.getConfigManager().getMessage("message-too-long",
                    "{max}", String.valueOf(maxLength)));
            return;
        }

        // Get target player (online or offline)
        Player targetPlayer = Bukkit.getPlayer(targetName);
        UUID targetUUID;
        String finalTargetName;

        if (targetPlayer != null) {
            targetUUID = targetPlayer.getUniqueId();
            finalTargetName = targetPlayer.getName();
        } else {
            // Try to get offline player
            targetUUID = Bukkit.getOfflinePlayer(targetName).getUniqueId();
            finalTargetName = targetName;

            // Check if player exists
            if (!Bukkit.getOfflinePlayer(targetUUID).hasPlayedBefore()) {
                sender.sendMessage(plugin.getConfigManager().getMessage("player-not-found",
                        "{player}", targetName));
                return;
            }
        }

        // Prevent self-send
        if (targetUUID.equals(sender.getUniqueId())) {
            sender.sendMessage(plugin.getConfigManager().getMessage("cannot-send-self"));
            return;
        }

        // Store pending mail
        PendingMail pendingMail = new PendingMail(sender.getUniqueId(), sender.getName(),
                targetUUID, finalTargetName, message);
        pendingMails.put(sender.getUniqueId(), pendingMail);

        // Send clickable message to add items
        Component addItemsComponent = plugin.getConfigManager()
                .getMessage("click-to-add-items")
                .clickEvent(ClickEvent.runCommand("/mail-additems"));

        sender.sendMessage(plugin.getConfigManager().getMessage("mail-pending",
                "{player}", finalTargetName));
        sender.sendMessage(addItemsComponent);

        // Auto-send after delay if no items added
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pendingMails.containsKey(sender.getUniqueId())) {
                sendPendingMail(sender);
            }
        }, 20L * 60); // 60 seconds timeout
    }

    /**
     * Opens the add items GUI for pending mail
     *
     * @param player the player
     */
    public void openAddItemsGUI(@NotNull Player player) {
        PendingMail pending = pendingMails.get(player.getUniqueId());
        if (pending == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-pending-mail"));
            return;
        }

        new AddItemsGUI(plugin, player, pending, this).open();
    }

    /**
     * Confirms and sends the pending mail
     *
     * @param player the sender
     */
    public void sendPendingMail(@NotNull Player player) {
        PendingMail pending = pendingMails.remove(player.getUniqueId());
        if (pending == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-pending-mail"));
            return;
        }

        // Send mail asynchronously
        plugin.getMailboxManager().sendMail(
                pending.senderUUID,
                pending.senderName,
                pending.recipientUUID,
                pending.recipientName,
                pending.message,
                pending.items
        ).thenAccept(mail -> {
            if (mail != null) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage(plugin.getConfigManager().getMessage("mail-sent",
                            "{player}", pending.recipientName));

                    // Notify recipient if online
                    Player recipient = Bukkit.getPlayer(pending.recipientUUID);
                    if (recipient != null) {
                        recipient.sendMessage(plugin.getConfigManager().getMessage("new-mail-notification",
                                "{sender}", pending.senderName));
                    }
                });
            } else {
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.sendMessage(plugin.getConfigManager().getMessage("mail-send-failed"))
                );
            }
        });
    }

    /**
     * Handles the /mail inbox command
     *
     * @param player the command sender
     */
    private void handleInbox(Player player) {
        new InboxGUI(plugin, player, 0).open();
    }

    /**
     * Handles the /mail clear command
     *
     * @param player the command sender
     */
    private void handleClear(@NotNull Player player) {
        plugin.getMailboxManager().deleteReadMail(player.getUniqueId())
                .thenAccept(count -> Bukkit.getScheduler().runTask(plugin, () -> {
                    if (count > 0) {
                        player.sendMessage(plugin.getConfigManager().getMessage("mail-cleared",
                                "{count}", String.valueOf(count)));
                    } else {
                        player.sendMessage(plugin.getConfigManager().getMessage("no-mail-to-clear"));
                    }
                }));
    }

    /**
     * Sends usage information to the player
     *
     * @param player the player
     */
    private void sendUsage(@NotNull Player player) {
        player.sendMessage(plugin.getConfigManager().getMessage("usage-header"));
        player.sendMessage(Component.text("/mail send <player> <message>").color(NamedTextColor.GRAY));
        player.sendMessage(Component.text("/mail inbox").color(NamedTextColor.GRAY));
        player.sendMessage(Component.text("/mail clear").color(NamedTextColor.GRAY));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("send");
            completions.add("inbox");
            completions.add("clear");
            completions.add("help");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("send")) {
            // Return online player names
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.equals(sender)) {
                    completions.add(player.getName());
                }
            }
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .toList();
    }

    /**
     * Represents a mail message waiting to be sent
     */
    public static class PendingMail {
        public final UUID senderUUID;
        public final String senderName;
        public final UUID recipientUUID;
        public final String recipientName;
        public final String message;
        public final List<org.bukkit.inventory.ItemStack> items;

        public PendingMail(UUID senderUUID, String senderName, UUID recipientUUID,
                           String recipientName, String message) {
            this.senderUUID = senderUUID;
            this.senderName = senderName;
            this.recipientUUID = recipientUUID;
            this.recipientName = recipientName;
            this.message = message;
            this.items = new ArrayList<>();
        }
    }
}