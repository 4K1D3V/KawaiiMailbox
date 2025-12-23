package dev.oumaimaa.commands;

import dev.oumaimaa.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Hidden command for opening the add items GUI
 *
 * <p>This command is triggered by clicking the "Add items" link
 * in chat when a mail is pending.</p>
 *
 * @author oumaimaa
 * @version 1.0.0
 */
public class AddItemsCommand implements CommandExecutor {

    private final Main plugin;
    private final MailCommand mailCommand;

    /**
     * Constructs a new add items command
     *
     * @param plugin the main plugin instance
     * @param mailCommand the mail command handler
     */
    public AddItemsCommand(Main plugin, MailCommand mailCommand) {
        this.plugin = plugin;
        this.mailCommand = mailCommand;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        mailCommand.openAddItemsGUI(player);
        return true;
    }
}