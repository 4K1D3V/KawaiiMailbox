package dev.oumaimaa.gui;

import dev.oumaimaa.Main;
import dev.oumaimaa.models.Mail;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GUI for displaying detailed mail information
 *
 * <p>This class creates an interface showing the full mail content,
 * attached items, and action buttons.</p>
 *
 * @author oumaimaa
 * @version 1.0.0
 */
public class MailDetailGUI implements Listener {

    private final Main plugin;
    private final Player player;
    private final Mail mail;
    private final InboxGUI previousGUI;
    private Inventory inventory;
    private final SimpleDateFormat dateFormat;

    /**
     * Constructs a new mail detail GUI
     *
     * @param plugin the main plugin instance
     * @param player the player viewing the mail
     * @param mail the mail to display
     * @param previousGUI the previous inbox GUI
     */
    public MailDetailGUI(Main plugin, Player player, Mail mail, InboxGUI previousGUI) {
        this.plugin = plugin;
        this.player = player;
        this.mail = mail;
        this.previousGUI = previousGUI;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
    }

    /**
     * Opens the mail detail GUI
     */
    public void open() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        ConfigurationSection guiConfig = plugin.getConfigManager().getGuiSection("mail-detail");
        String title = guiConfig.getString("title", "Mail Details");
        int size = guiConfig.getInt("size", 54);

        inventory = Bukkit.createInventory(null, size, Component.text(title));
        populateInventory();
        player.openInventory(inventory);

        // Mark as read
        if (!mail.isRead()) {
            plugin.getMailboxManager().markAsRead(mail.getId());
            mail.setRead(true);
        }
    }

    /**
     * Populates the inventory with mail details and action buttons
     */
    private void populateInventory() {
        ConfigurationSection guiConfig = plugin.getConfigManager().getGuiSection("mail-detail");

        addMailInfo(guiConfig);
        addAttachedItems(guiConfig);
        addActionButtons(guiConfig);
        fillEmptySlots(guiConfig);
    }

    /**
     * Adds the mail information display item
     *
     * @param guiConfig the GUI configuration section
     */
    private void addMailInfo(@NotNull ConfigurationSection guiConfig) {
        Material material = Material.valueOf(
                guiConfig.getString("info-item.material", "WRITABLE_BOOK"));
        int slot = guiConfig.getInt("info-item.slot", 4);

        ItemStack infoItem = new ItemStack(material);
        ItemMeta meta = infoItem.getItemMeta();

        meta.displayName(Component.text("Mail from " + mail.getSenderName())
                .color(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("From: " + mail.getSenderName())
                .color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Date: " + dateFormat.format(new Date(mail.getTimestamp())))
                .color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Message:").color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));

        // Split message into lines
        String[] words = mail.getMessage().split(" ");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            if (line.length() + word.length() > 30) {
                lore.add(Component.text(line.toString())
                        .color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
                line = new StringBuilder(word).append(" ");
            } else {
                line.append(word).append(" ");
            }
        }
        if (!line.isEmpty()) {
            lore.add(Component.text(line.toString())
                    .color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        }

        meta.lore(lore);
        infoItem.setItemMeta(meta);
        inventory.setItem(slot, infoItem);
    }

    /**
     * Adds attached items to the display
     *
     * @param guiConfig the GUI configuration section
     */
    private void addAttachedItems(ConfigurationSection guiConfig) {
        if (!mail.hasItems() || mail.isItemsClaimed()) {
            return;
        }

        AtomicInteger startSlot = new AtomicInteger(guiConfig.getInt("items-start-slot", 19));
        int slot = startSlot.get();

        for (ItemStack item : mail.getItems()) {
            if (slot >= 35) break; // Don't overflow into button area
            inventory.setItem(slot, item);
            slot++;
        }
    }

    /**
     * Adds action buttons to the inventory
     *
     * @param guiConfig the GUI configuration section
     */
    private void addActionButtons(ConfigurationSection guiConfig) {
        // Claim items button (if items exist and not claimed)
        if (mail.hasItems() && !mail.isItemsClaimed()) {
            ItemStack claimButton = createButton(
                    Material.valueOf(guiConfig.getString("claim-button.material", "CHEST")),
                    guiConfig.getString("claim-button.name", "Claim Items"),
                    guiConfig.getStringList("claim-button.lore")
            );
            inventory.setItem(guiConfig.getInt("claim-button.slot", 48), claimButton);
        }

        // Back button
        ItemStack backButton = createButton(
                Material.valueOf(guiConfig.getString("back-button.material", "ARROW")),
                guiConfig.getString("back-button.name", "Back to Inbox"),
                guiConfig.getStringList("back-button.lore")
        );
        inventory.setItem(guiConfig.getInt("back-button.slot", 45), backButton);

        // Close button
        ItemStack closeButton = createButton(
                Material.valueOf(guiConfig.getString("close-button.material", "BARRIER")),
                guiConfig.getString("close-button.name", "Close"),
                guiConfig.getStringList("close-button.lore")
        );
        inventory.setItem(guiConfig.getInt("close-button.slot", 49), closeButton);
    }

    /**
     * Creates a GUI button ItemStack
     *
     * @param material the button material
     * @param name the button name
     * @param loreList the button lore
     * @return the ItemStack
     */
    private @NotNull ItemStack createButton(Material material, String name, List<String> loreList) {
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();

        meta.displayName(Component.text(name)
                .color(NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));

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
     * Fills empty slots with a filler item
     *
     * @param guiConfig the GUI configuration section
     */
    private void fillEmptySlots(@NotNull ConfigurationSection guiConfig) {
        if (!guiConfig.getBoolean("fill-empty-slots", true)) {
            return;
        }

        Material fillerMaterial = Material.valueOf(
                guiConfig.getString("filler-material", "GRAY_STAINED_GLASS_PANE"));
        ItemStack filler = new ItemStack(fillerMaterial);
        ItemMeta meta = filler.getItemMeta();
        meta.displayName(Component.empty());
        filler.setItemMeta(meta);

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player clicker)) {
            return;
        }

        if (!clicker.equals(player)) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        ConfigurationSection guiConfig = plugin.getConfigManager().getGuiSection("mail-detail");
        int slot = event.getSlot();

        // Handle back button
        if (slot == guiConfig.getInt("back-button.slot", 45)) {
            clicker.closeInventory();
            previousGUI.open();
            return;
        }

        // Handle close button
        if (slot == guiConfig.getInt("close-button.slot", 49)) {
            clicker.closeInventory();
            return;
        }

        // Handle claim items button
        if (slot == guiConfig.getInt("claim-button.slot", 48)
                && mail.hasItems() && !mail.isItemsClaimed()) {
            handleClaimItems(clicker);
        }
    }

    /**
     * Handles claiming attached items
     *
     * @param clicker the player claiming items
     */
    private void handleClaimItems(@NotNull Player clicker) {
        List<ItemStack> items = mail.getItems();

        // Check if player has enough inventory space
        int emptySlots = 0;
        for (ItemStack item : clicker.getInventory().getStorageContents()) {
            if (item == null || item.getType() == Material.AIR) {
                emptySlots++;
            }
        }

        if (emptySlots < items.size()) {
            clicker.sendMessage(plugin.getConfigManager().getMessage("inventory-full"));
            return;
        }

        // Give items to player
        for (ItemStack item : items) {
            clicker.getInventory().addItem(item);
        }

        // Mark items as claimed
        plugin.getMailboxManager().markItemsClaimed(mail.getId()).thenAccept(success -> {
            if (success) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    clicker.sendMessage(plugin.getConfigManager().getMessage("items-claimed"));
                    clicker.closeInventory();
                });
            }
        });
    }

    @EventHandler
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            InventoryClickEvent.getHandlerList().unregister(this);
            InventoryCloseEvent.getHandlerList().unregister(this);
        }
    }
}