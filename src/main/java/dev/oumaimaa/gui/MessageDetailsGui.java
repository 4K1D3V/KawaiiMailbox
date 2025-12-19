package dev.oumaimaa.gui;

import dev.oumaimaa.Main;
import dev.oumaimaa.model.MailMessage;
import dev.oumaimaa.repository.MailRepository;
import dev.oumaimaa.util.TimeFormatter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.slimecraft.bedrock.menu.Menu;
import org.slimecraft.bedrock.menu.button.Button;
import org.slimecraft.bedrock.util.item.ItemBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * GUI for displaying detailed information about a specific mail message.
 * Allows marking as read and claiming items.
 */
public final class MessageDetailsGui {

    private final Player player;
    private final MailMessage mail;
    private final int returnPage;
    private final Main plugin = Main.getInstance();
    private final MailRepository repository = new MailRepository(plugin);
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MessageDetailsGui(Player player, MailMessage mail, int returnPage) {
        this.player = player;
        this.mail = mail;
        this.returnPage = returnPage;
    }

    /**
     * Opens the message details GUI.
     */
    public void open() {
        Menu menu = Menu.Inventory.chest(6)
                .name(miniMessage.deserialize(plugin.getMainConfig().getMessageDetailsGuiTitle()))
                .build();

        // Message display
        addMessageDisplay(menu);

        // Action buttons
        addActionButtons(menu);

        // Items display
        if (mail.hasItems() && !mail.isItemsClaimed()) {
            addItemsDisplay(menu);
        }

        menu.show(player);
    }

    /**
     * Adds the message display item.
     */
    private void addMessageDisplay(Menu menu) {
        String loreTemplate = plugin.getMessagesConfig().getMessageDisplayLore()
                .replace("{message}", mail.getMessage())
                .replace("{sender}", mail.getSenderName())
                .replace("{time}", TimeFormatter.format(mail.getTimestamp()));

        List<String> loreLines = Arrays.asList(loreTemplate.split("\\|"));

        ItemStack messageItem = ItemBuilder.create()
                .material(Material.WRITABLE_BOOK)
                .name(plugin.getMessagesConfig().getMessageDisplayName())
                .loreString(loreLines)
                .build();

        Button messageButton = Button.builder()
                .item(messageItem)
                .slot(13)
                .build();

        menu.addButton(messageButton);
    }

    /**
     * Adds action buttons (mark as read, claim items, back).
     */
    private void addActionButtons(Menu menu) {
        // Mark as read button
        if (mail.isUnread()) {
            ItemStack markReadItem = ItemBuilder.create()
                    .material(Material.GREEN_WOOL)
                    .name(plugin.getMessagesConfig().getMessageDetailsMarkReadName())
                    .loreString(Arrays.asList(
                            plugin.getMessagesConfig().getMessageDetailsMarkReadLore().split("\\|")
                    ))
                    .build();

            Button markReadButton = Button.builder()
                    .item(markReadItem)
                    .slot(47)
                    .leftClicked(context -> {
                        repository.updateStatus(mail.getId(), MailMessage.MailStatus.READ);
                        player.sendMessage(miniMessage.deserialize(
                                plugin.getMessagesConfig().getMessageMarkedRead()
                        ));
                        player.closeInventory();

                        // Refresh the mail object
                        MailMessage updatedMail = repository.getMailById(mail.getId());
                        if (updatedMail != null) {
                            new MessageDetailsGui(player, updatedMail, returnPage).open();
                        }
                    })
                    .build();

            menu.addButton(markReadButton);
        }

        // Claim items button
        if (mail.hasItems() && !mail.isItemsClaimed()) {
            ItemStack claimItem = ItemBuilder.create()
                    .material(Material.CHEST)
                    .name(plugin.getMessagesConfig().getMessageDetailsClaimItemsName())
                    .loreString(Arrays.asList(
                            plugin.getMessagesConfig().getMessageDetailsClaimItemsLore().split("\\|")
                    ))
                    .build();

            Button claimButton = Button.builder()
                    .item(claimItem)
                    .slot(49)
                    .leftClicked(context -> {
                        claimItems();
                    })
                    .build();

            menu.addButton(claimButton);
        } else if (mail.hasItems() && mail.isItemsClaimed()) {
            ItemStack claimedItem = ItemBuilder.create()
                    .material(Material.GRAY_WOOL)
                    .name("<gray>Items Already Claimed</gray>")
                    .build();

            Button claimedButton = Button.builder()
                    .item(claimedItem)
                    .slot(49)
                    .build();

            menu.addButton(claimedButton);
        }

        // Back button
        ItemStack backItem = ItemBuilder.create()
                .material(Material.BARRIER)
                .name(plugin.getMessagesConfig().getMessageDetailsBackName())
                .build();

        Button backButton = Button.builder()
                .item(backItem)
                .slot(51)
                .leftClicked(context -> {
                    player.closeInventory();
                    new InboxGui(player, returnPage).open();
                })
                .build();

        menu.addButton(backButton);
    }

    /**
     * Adds items preview display.
     */
    private void addItemsDisplay(Menu menu) {
        int startSlot = 19;
        int maxDisplay = 7;

        List<ItemStack> items = mail.getItems();
        for (int i = 0; i < Math.min(items.size(), maxDisplay); i++) {
            ItemStack displayItem = items.get(i).clone();

            Button itemButton = Button.builder()
                    .item(displayItem)
                    .slot(startSlot + i)
                    .build();

            menu.addButton(itemButton);
        }

        if (items.size() > maxDisplay) {
            ItemStack moreItem = ItemBuilder.create()
                    .material(Material.CHEST)
                    .name("<yellow>+" + (items.size() - maxDisplay) + " more items</yellow>")
                    .build();

            Button moreButton = Button.builder()
                    .item(moreItem)
                    .slot(startSlot + maxDisplay)
                    .build();

            menu.addButton(moreButton);
        }
    }

    /**
     * Claims items from the mail and gives them to the player.
     */
    private void claimItems() {
        if (!mail.hasItems()) {
            player.sendMessage(miniMessage.deserialize(
                    plugin.getMessagesConfig().getMessageNoItems()
            ));
            return;
        }

        if (mail.isItemsClaimed()) {
            player.sendMessage(miniMessage.deserialize(
                    plugin.getMessagesConfig().getMessageAlreadyClaimed()
            ));
            return;
        }

        // Give items to player
        for (ItemStack item : mail.getItems()) {
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            } else {
                player.getInventory().addItem(item);
            }
        }

        // Mark as claimed
        repository.markItemsClaimed(mail.getId());

        player.sendMessage(miniMessage.deserialize(
                plugin.getMessagesConfig().getMessageItemsClaimed()
        ));

        player.closeInventory();

        // Refresh GUI
        MailMessage updatedMail = repository.getMailById(mail.getId());
        if (updatedMail != null) {
            new MessageDetailsGui(player, updatedMail, returnPage).open();
        }
    }
}