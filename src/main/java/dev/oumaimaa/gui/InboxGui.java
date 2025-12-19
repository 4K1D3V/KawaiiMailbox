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
 * GUI for displaying a player's mail inbox with pagination.
 */
public final class InboxGui {

    private final Player player;
    private final int page;
    private final Main plugin = Main.getInstance();
    private final MailRepository repository = new MailRepository(plugin);
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public InboxGui(Player player, int page) {
        this.player = player;
        this.page = page;
    }

    /**
     * Opens the inbox GUI for the player.
     */
    public void open() {
        List<MailMessage> allMail = repository.getMailByRecipient(player.getUniqueId());

        if (allMail.isEmpty()) {
            player.sendMessage(miniMessage.deserialize(
                    plugin.getMessagesConfig().getMailInboxEmpty()
            ));
            return;
        }

        int messagesPerPage = plugin.getMainConfig().getMessagesPerPage();
        int totalPages = (int) Math.ceil((double) allMail.size() / messagesPerPage);

        // Ensure page is within bounds
        int currentPage = Math.max(0, Math.min(page, totalPages - 1));

        // Get messages for current page
        int startIndex = currentPage * messagesPerPage;
        int endIndex = Math.min(startIndex + messagesPerPage, allMail.size());
        List<MailMessage> pageMessages = allMail.subList(startIndex, endIndex);

        // Build GUI
        Menu menu = Menu.Inventory.chest(plugin.getMainConfig().getInboxGuiRows())
                .name(miniMessage.deserialize(plugin.getMainConfig().getInboxGuiTitle()))
                .build();

        // Add mail messages
        int slot = 0;
        for (MailMessage mail : pageMessages) {
            if (slot >= messagesPerPage) break;

            Material material = mail.isUnread() ? Material.PAPER : Material.MAP;
            String nameTemplate = mail.isUnread()
                    ? plugin.getMessagesConfig().getInboxItemUnreadName()
                    : plugin.getMessagesConfig().getInboxItemReadName();

            String statusColor = mail.isUnread() ? "<green>UNREAD</green>" : "<gray>READ</gray>";

            String loreTemplate = plugin.getMessagesConfig().getInboxItemLore()
                    .replace("{sender}", mail.getSenderName())
                    .replace("{preview}", mail.getPreview())
                    .replace("{time}", TimeFormatter.format(mail.getTimestamp()))
                    .replace("{status}", statusColor);

            List<String> loreLines = Arrays.asList(loreTemplate.split("\\|"));

            ItemStack item = ItemBuilder.create()
                    .material(material)
                    .name(nameTemplate)
                    .loreString(loreLines)
                    .build();

            Button button = Button.builder()
                    .item(item)
                    .slot(slot)
                    .leftClicked(context -> {
                        player.closeInventory();
                        MessageDetailsGui detailsGui = new MessageDetailsGui(player, mail, currentPage);
                        detailsGui.open();
                    })
                    .build();

            menu.addButton(button);
            slot++;
        }

        // Add pagination buttons
        addPaginationButtons(menu, currentPage, totalPages);

        menu.show(player);
    }

    /**
     * Adds pagination buttons to the menu.
     *
     * @param menu        the menu
     * @param currentPage the current page
     * @param totalPages  the total number of pages
     */
    private void addPaginationButtons(Menu menu, int currentPage, int totalPages) {
        int guiSize = plugin.getMainConfig().getInboxGuiRows() * 9;

        // Previous button
        if (currentPage > 0) {
            ItemStack prevItem = ItemBuilder.create()
                    .material(Material.ARROW)
                    .name(plugin.getMessagesConfig().getPaginationPreviousName())
                    .build();

            Button prevButton = Button.builder()
                    .item(prevItem)
                    .slot(guiSize - 9)
                    .leftClicked(context -> {
                        player.closeInventory();
                        new InboxGui(player, currentPage - 1).open();
                    })
                    .build();

            menu.addButton(prevButton);
        }

        // Page info
        ItemStack infoItem = ItemBuilder.create()
                .material(Material.BOOK)
                .name(plugin.getMessagesConfig().getPaginationInfoName()
                        .replace("{current}", String.valueOf(currentPage + 1))
                        .replace("{total}", String.valueOf(totalPages)))
                .build();

        Button infoButton = Button.builder()
                .item(infoItem)
                .slot(guiSize - 5)
                .build();

        menu.addButton(infoButton);

        // Next button
        if (currentPage < totalPages - 1) {
            ItemStack nextItem = ItemBuilder.create()
                    .material(Material.ARROW)
                    .name(plugin.getMessagesConfig().getPaginationNextName())
                    .build();

            Button nextButton = Button.builder()
                    .item(nextItem)
                    .slot(guiSize - 1)
                    .leftClicked(context -> {
                        player.closeInventory();
                        new InboxGui(player, currentPage + 1).open();
                    })
                    .build();

            menu.addButton(nextButton);
        }
    }
}