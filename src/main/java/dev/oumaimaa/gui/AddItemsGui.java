package dev.oumaimaa.gui;

import dev.oumaimaa.Main;
import dev.oumaimaa.repository.MailRepository;
import dev.oumaimaa.session.MailSession;
import dev.oumaimaa.session.SessionManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.slimecraft.bedrock.menu.Menu;
import org.slimecraft.bedrock.menu.button.Button;
import org.slimecraft.bedrock.util.item.ItemBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * GUI for adding items to a mail message before sending.
 */
public final class AddItemsGui {

    private final Player player;
    private final MailSession session;
    private final Main plugin = Main.getInstance();
    private final MailRepository repository = new MailRepository(plugin);
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public AddItemsGui(Player player, MailSession session) {
        this.player = player;
        this.session = session;
    }

    /**
     * Opens the add items GUI.
     */
    public void open() {
        Menu menu = Menu.Inventory.chest(plugin.getMainConfig().getAddItemsGuiRows())
                .name(miniMessage.deserialize(plugin.getMainConfig().getAddItemsGuiTitle()))
                .build();

        // Add info item
        addInfoItem(menu);

        // Add action buttons
        addActionButtons(menu);

        // Add item slots (moveable)
        addItemSlots(menu);

        menu.show(player);
    }

    /**
     * Adds the information display item.
     */
    private void addInfoItem(Menu menu) {
        ItemStack infoItem = ItemBuilder.create()
                .material(Material.BOOK)
                .name(plugin.getMessagesConfig().getAddItemsInfoName())
                .loreString(Arrays.asList(
                        plugin.getMessagesConfig().getAddItemsInfoLore().split("\\|")
                ))
                .build();

        Button infoButton = Button.builder()
                .item(infoItem)
                .slot(4)
                .build();

        menu.addButton(infoButton);
    }

    /**
     * Adds action buttons (confirm, cancel).
     */
    private void addActionButtons(Menu menu) {
        int guiSize = plugin.getMainConfig().getAddItemsGuiRows() * 9;

        // Confirm button
        ItemStack confirmItem = ItemBuilder.create()
                .material(Material.LIME_WOOL)
                .name(plugin.getMessagesConfig().getAddItemsConfirmName())
                .loreString(Arrays.asList(
                        plugin.getMessagesConfig().getAddItemsConfirmLore().split("\\|")
                ))
                .build();

        Button confirmButton = Button.builder()
                .item(confirmItem)
                .slot(guiSize - 9)
                .leftClicked(context -> {
                    confirmAndSend();
                })
                .build();

        menu.addButton(confirmButton);

        // Cancel button
        ItemStack cancelItem = ItemBuilder.create()
                .material(Material.RED_WOOL)
                .name(plugin.getMessagesConfig().getAddItemsCancelName())
                .loreString(Arrays.asList(
                        plugin.getMessagesConfig().getAddItemsCancelLore().split("\\|")
                ))
                .build();

        Button cancelButton = Button.builder()
                .item(cancelItem)
                .slot(guiSize - 1)
                .leftClicked(context -> {
                    SessionManager.removeSession(player);
                    player.closeInventory();
                })
                .build();

        menu.addButton(cancelButton);
    }

    /**
     * Adds moveable item slots for players to place items.
     */
    private void addItemSlots(Menu menu) {
        int maxItems = plugin.getMainConfig().getMaxItemsPerMessage();
        int startSlot = 9;
        int endSlot = startSlot + maxItems;

        // Create empty slots that are moveable
        for (int slot = startSlot; slot < endSlot && slot < 45; slot++) {
            Button emptyButton = Button.builder()
                    .item(new ItemStack(Material.AIR))
                    .slot(slot)
                    .moveable()
                    .build();

            menu.addButton(emptyButton);
        }
    }

    /**
     * Confirms the item selection and sends the mail.
     */
    private void confirmAndSend() {
        // Collect items from GUI
        List<ItemStack> items = new ArrayList<>();

        for (ItemStack item : player.getOpenInventory().getTopInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;

            // Skip control items (first row and last row)
            int slot = player.getOpenInventory().getTopInventory().first(item);
            if (slot < 9 || slot >= 45) continue;

            items.add(item.clone());
        }

        // Add items to mail
        if (!items.isEmpty()) {
            session.getMail().setItems(items);
        }

        // Save to database
        boolean success = repository.saveMail(session.getMail());

        if (success) {
            player.sendMessage(miniMessage.deserialize(
                    plugin.getMessagesConfig().getMailSendItemsAdded()
                            .replace("{player}", session.getMail().getRecipientName())
            ));
        } else {
            player.sendMessage(miniMessage.deserialize(
                    plugin.getMessagesConfig().getErrorInternalError()
            ));
        }

        // Cleanup
        SessionManager.removeSession(player);
        player.closeInventory();
    }
}