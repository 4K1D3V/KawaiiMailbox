package dev.oumaimaa.command;

import dev.oumaimaa.Main;
import dev.oumaimaa.gui.AddItemsGui;
import dev.oumaimaa.gui.InboxGui;
import dev.oumaimaa.model.MailMessage;
import dev.oumaimaa.repository.MailRepository;
import dev.oumaimaa.session.MailSession;
import dev.oumaimaa.session.SessionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main command handler for the mailbox system.
 * Handles /mail send, /mail inbox, and /mail clear commands.
 *
 * <p>Implements both {@link CommandExecutor} and {@link TabCompleter} for complete
 * command functionality with proper tab completion support.</p>
 */
public final class MailCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = Arrays.asList("send", "inbox", "clear");

    private final Main plugin = Main.getInstance();
    private final MailRepository repository = new MailRepository(plugin);
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    /**
     * Registers the command with Bukkit's command system.
     */
    public void register() {
        var command = plugin.getCommand("mail");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        } else {
            plugin.getLogger().severe("Failed to register /mail command!");
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        return switch (args[0].toLowerCase()) {
            case "send" -> handleSend(player, args);
            case "inbox" -> handleInbox(player);
            case "clear" -> handleClear(player);
            case "additems" -> handleAddItems(player);
            default -> {
                sendUsage(player);
                yield true;
            }
        };
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                               @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        // First argument: subcommand
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return SUBCOMMANDS.stream()
                    .filter(sub -> sub.startsWith(input))
                    .collect(Collectors.toList());
        }

        // Second argument for "send": player name
        if (args.length == 2 && args[0].equalsIgnoreCase("send")) {
            String input = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .filter(name -> !name.equals(sender.getName()))
                    .collect(Collectors.toList());
        }

        // Third argument and beyond for "send": message hint
        if (args.length >= 3 && args[0].equalsIgnoreCase("send")) {
            return List.of("<message>");
        }

        return new ArrayList<>();
    }

    /**
     * Sends command usage information to the player.
     *
     * @param player the player to send usage to
     */
    private void sendUsage(@NotNull Player player) {
        player.sendMessage(miniMessage.deserialize(
                plugin.getMessagesConfig().getPrefix() + "<yellow>Usage:</yellow>"
        ));
        player.sendMessage(miniMessage.deserialize(
                "<gray>  /mail send <player> <message></gray>"
        ));
        player.sendMessage(miniMessage.deserialize(
                "<gray>  /mail inbox</gray>"
        ));
        player.sendMessage(miniMessage.deserialize(
                "<gray>  /mail clear</gray>"
        ));
    }

    /**
     * Handles /mail send <player> <message>
     *
     * @param player the player executing the command
     * @param args   command arguments
     * @return true if command was handled successfully
     */
    private boolean handleSend(@NotNull Player player, String[] args) {
        if (!player.hasPermission("kawaiimailbox.send")) {
            player.sendMessage(miniMessage.deserialize(
                    plugin.getMessagesConfig().getErrorNoPermission()
            ));
            return true;
        }

        if (!plugin.getMongoManager().isConnected()) {
            player.sendMessage(miniMessage.deserialize(
                    plugin.getMessagesConfig().getErrorMongodbUnavailable()
            ));
            return true;
        }

        if (args.length < 3) {
            player.sendMessage(miniMessage.deserialize(
                    plugin.getMessagesConfig().getErrorInvalidUsage()
                            .replace("{usage}", "/mail send <player> <message>")
            ));
            return true;
        }

        String targetName = args[1];
        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage(miniMessage.deserialize(
                    plugin.getMessagesConfig().getErrorPlayerNotFound()
                            .replace("{player}", targetName)
            ));
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(miniMessage.deserialize(
                    plugin.getMessagesConfig().getMailSendSelf()
            ));
            return true;
        }

        MailMessage mail = new MailMessage(
                target.getUniqueId(),
                target.getName(),
                player.getUniqueId(),
                player.getName(),
                message
        );

        SessionManager.createSession(player, mail);

        Component prompt = miniMessage.deserialize(
                plugin.getMessagesConfig().getMailSendAddItemsPrompt()
        ).clickEvent(ClickEvent.runCommand("/mail additems"));

        player.sendMessage(prompt);
        return true;
    }

    /**
     * Handles /mail additems (internal command triggered by clicking prompt)
     *
     * @param player the player executing the command
     * @return true if command was handled successfully
     */
    private boolean handleAddItems(Player player) {
        MailSession session = SessionManager.getSession(player);
        if (session == null) {
            player.sendMessage(miniMessage.deserialize(
                    plugin.getMessagesConfig().getPrefix() +
                            "<red>No active mail session found!</red>"
            ));
            return true;
        }

        AddItemsGui gui = new AddItemsGui(player, session);
        gui.open();
        return true;
    }

    /**
     * Handles /mail inbox
     *
     * @param player the player executing the command
     * @return true if command was handled successfully
     */
    private boolean handleInbox(@NotNull Player player) {
        if (!player.hasPermission("kawaiimailbox.inbox")) {
            player.sendMessage(miniMessage.deserialize(
                    plugin.getMessagesConfig().getErrorNoPermission()
            ));
            return true;
        }

        if (!plugin.getMongoManager().isConnected()) {
            player.sendMessage(miniMessage.deserialize(
                    plugin.getMessagesConfig().getErrorMongodbUnavailable()
            ));
            return true;
        }

        player.sendMessage(miniMessage.deserialize(
                plugin.getMessagesConfig().getMailInboxOpened()
        ));

        InboxGui gui = new InboxGui(player, 0);
        gui.open();
        return true;
    }

    /**
     * Handles /mail clear
     *
     * @param player the player executing the command
     * @return true if command was handled successfully
     */
    private boolean handleClear(@NotNull Player player) {
        if (!player.hasPermission("kawaiimailbox.clear")) {
            player.sendMessage(miniMessage.deserialize(
                    plugin.getMessagesConfig().getErrorNoPermission()
            ));
            return true;
        }

        if (!plugin.getMongoManager().isConnected()) {
            player.sendMessage(miniMessage.deserialize(
                    plugin.getMessagesConfig().getErrorMongodbUnavailable()
            ));
            return true;
        }

        long deleted = repository.deleteReadMessages(player.getUniqueId());

        if (deleted > 0) {
            player.sendMessage(miniMessage.deserialize(
                    plugin.getMessagesConfig().getMailClearSuccess()
                            .replace("{count}", String.valueOf(deleted))
            ));
        } else {
            player.sendMessage(miniMessage.deserialize(
                    plugin.getMessagesConfig().getMailClearNone()
            ));
        }
        return true;
    }
}