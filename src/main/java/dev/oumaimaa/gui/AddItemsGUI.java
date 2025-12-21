package dev.oumaimaa.gui;

import dev.oumaimaa.Main;
import dev.oumaimaa.commands.MailCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for adding items to a mail message
 *
 * <p>This class creates an interface where players can add items
 * to their pending mail before sending.</p>
 *
 * @author Oumaimaa
 * @version 1.0.0
 */
public class AddItemsGUI implements Listener {

    private final Main plugin;
    private final Player player;
    private final MailCommand.PendingMail pendingMail;
    private final MailCommand mailCommand;
    private Inventory inventory;

    /**
     * Constructs a new add items GUI
     *
     * @param plugin the main plugin instance
     * @param player the player adding items
     * @param pendingMail the pending mail to add items to
     * @param mailCommand the mail command handler
     */
    public AddItemsGUI(Main plugin, Player player, MailCommand.PendingMail pendingMail,
                       MailCommand mailCommand) {
        this.plugin = plugin;
        this.player = player;
        this.pendingMail = pendingMail;
        this.mailCommand = mailCommand;
    }

    /**
     * Opens the add items GUI
     */
    public void open() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        ConfigurationSection guiConfig = plugin.getConfigManager().getGuiSection("add-items");
        String title = guiConfig.getString("title", "Add Items to Mail");
        int size = guiConfig.getInt("size", 54);

        inventory = Bukkit.createInventory(null, size, Component.text(title));
        populateInventory();
        player.openInventory(inventory);
    }

    /**
     * Populates the inventory with instruction items and action buttons
     */
    private void populateInventory() {
        ConfigurationSection guiConfig = plugin.getConfigManager().getGuiSection("add-items");

        addInstructionItem(guiConfig);
        addActionButtons(guiConfig);
        fillBorderSlots(guiConfig);
    }

    /**
     * Adds an instruction item to guide the player
     *
     * @param guiConfig the GUI configuration section
     */
    private void addInstructionItem(@NotNull ConfigurationSection guiConfig) {
        Material material = Material.valueOf(
                guiConfig.getString("instruction-item.material", "BOOK"));
        int slot = guiConfig.getInt("instruction-item.slot", 4);

        ItemStack instructionItem = new ItemStack(material);
        ItemMeta meta = instructionItem.getItemMeta();

        meta.displayName(Component.text("How to add items")
                .color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Place items you want to send")
                .color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("in the empty slots.")
                .color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Click 'Confirm' to send the mail")
                .color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("with the items, or 'Cancel' to")
                .color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("return items and cancel.")
                .color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        instructionItem.setItemMeta(meta);
        inventory.setItem(slot, instructionItem);
    }

    /**
     * Adds action buttons to the inventory
     *
     * @param guiConfig the GUI configuration section
     */
    private void addActionButtons(@NotNull ConfigurationSection guiConfig) {
        // Confirm button
        ItemStack confirmButton = createButton(
                Material.valueOf(guiConfig.getString("confirm-button.material", "LIME_CONCRETE")),
                guiConfig.getString("confirm-button.name", "Confirm and Send"),
                guiConfig.getStringList("confirm-button.lore"),
                NamedTextColor.GREEN
        );
        inventory.setItem(guiConfig.getInt("confirm-button.slot", 48), confirmButton);

        // Cancel button
        ItemStack cancelButton = createButton(
                Material.valueOf(guiConfig.getString("cancel-button.material", "RED_CONCRETE")),
                guiConfig.getString("cancel-button.name", "Cancel"),
                guiConfig.getStringList("cancel-button.lore"),
                NamedTextColor.RED
        );
        inventory.setItem(guiConfig.getInt("cancel-button.slot", 50), cancelButton);
    }

    /**
     * Creates a GUI button ItemStack
     *
     * @param material the button material
     * @param name the button name
     * @param loreList the button lore
     * @param color the button color
     * @return the ItemStack
     */
    private @NotNull ItemStack createButton(Material material, String name, List<String> loreList,
                                            NamedTextColor color) {
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();

        meta.displayName(Component.text(name)
                .color(color).decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));

        if (loreList != null && !loreList.isEmpty()) {
            List<Component> lore = new ArrayList<>();
            for (String line : loreList) {
                lore.add(Component.text(line)
                        .color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(lore);
        }

        button.setItemMeta(meta);
        return button;
    }

    /**
     * Fills border slots with a glass pane
     *
     * @param guiConfig the GUI configuration section
     */
    private void fillBorderSlots(@NotNull ConfigurationSection guiConfig) {
        Material borderMaterial = Material.valueOf(
                guiConfig.getString("border-material", "BLACK_STAINED_GLASS_PANE"));
        ItemStack border = new ItemStack(borderMaterial);
        ItemMeta meta = border.getItemMeta();
        meta.displayName(Component.empty());
        border.setItemMeta(meta);

        // Fill top and bottom rows
        for (int i = 0; i < 9; i++) {
            if (i != 4) { // Skip instruction item slot
                inventory.setItem(i, border);
            }
            inventory.setItem(45 + i, border);
        }

        // Fill side columns
        for (int i = 1; i < 5; i++) {
            inventory.setItem(i * 9, border);
            inventory.setItem(i * 9 + 8, border);
        }
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player clicker)) {
            return;
        }

        if (!clicker.equals(player)) {
            return;
        }

        int slot = event.getSlot();
        ConfigurationSection guiConfig = plugin.getConfigManager().getGuiSection("add-items");

        // Handle confirm button
        if (slot == guiConfig.getInt("confirm-button.slot", 48)) {
            event.setCancelled(true);
            handleConfirm();
            return;
        }

        // Handle cancel button
        if (slot == guiConfig.getInt("cancel-button.slot", 50)) {
            event.setCancelled(true);
            handleCancel();
            return;
        }

        // Prevent interaction with border and instruction slots
        if (isBorderSlot(slot) || slot == guiConfig.getInt("instruction-item.slot", 4)) {
            event.setCancelled(true);
        }

        // Allow item placement in valid slots
        // No need to cancel, let players add/remove items freely
    }

    /**
     * Checks if a slot is a border slot
     *
     * @param slot the slot to check
     * @return true if it's a border slot, false otherwise
     */
    private boolean isBorderSlot(int slot) {
        // Top and bottom rows
        if (slot < 9 || slot >= 45) {
            return true;
        }
        // Side columns
        return slot % 9 == 0 || slot % 9 == 8;
    }

    /**
     * Handles the confirmation action
     */
    private void handleConfirm() {
        // Collect all items from the inventory
        pendingMail.items.clear();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (!isBorderSlot(i) && i != plugin.getConfigManager()
                    .getGuiSection("add-items").getInt("instruction-item.slot", 4)) {
                ItemStack item = inventory.getItem(i);
                if (item != null && item.getType() != Material.AIR) {
                    pendingMail.items.add(item.clone());
                }
            }
        }

        player.closeInventory();
        mailCommand.sendPendingMail(player, !pendingMail.items.isEmpty());
    }

    /**
     * Handles the cancel action
     */
    private void handleCancel() {
        // Return all items to player
        for (int i = 0; i < inventory.getSize(); i++) {
            if (!isBorderSlot(i) && i != plugin.getConfigManager()
                    .getGuiSection("add-items").getInt("instruction-item.slot", 4)) {
                ItemStack item = inventory.getItem(i);
                if (item != null && item.getType() != Material.AIR) {
                    player.getInventory().addItem(item);
                }
            }
        }

        player.closeInventory();
        player.sendMessage(plugin.getConfigManager().getMessage("mail-cancelled"));
    }

    @EventHandler
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            InventoryClickEvent.getHandlerList().unregister(this);
            InventoryCloseEvent.getHandlerList().unregister(this);
        }
    }
}