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

/**
 * GUI for displaying player's inbox with pagination
 *
 * <p>This class creates and manages the interactive inbox interface where
 * players can view their received mail messages.</p>
 *
 * @author oumaimaa
 * @version 1.0.0
 */
public class InboxGUI implements Listener {

    private final Main plugin;
    private final Player player;
    private final int page;
    private Inventory inventory;
    private final List<Mail> currentMails;
    private final SimpleDateFormat dateFormat;

    /**
     * Constructs a new inbox GUI
     *
     * @param plugin the main plugin instance
     * @param player the player viewing the inbox
     * @param page the current page number
     */
    public InboxGUI(Main plugin, Player player, int page) {
        this.plugin = plugin;
        this.player = player;
        this.page = page;
        this.currentMails = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
    }

    /**
     * Opens the inbox GUI for the player
     */
    public void open() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        ConfigurationSection guiConfig = plugin.getConfigManager().getGuiSection("inbox");
        String title = guiConfig.getString("title", "Mailbox");
        int size = guiConfig.getInt("size", 54);

        inventory = Bukkit.createInventory(null, size, Component.text(title));

        // Load mail asynchronously
        int messagesPerPage = plugin.getConfigManager().getMessagesPerPage();
        plugin.getMailboxManager().getInbox(player.getUniqueId(), page, messagesPerPage)
                .thenAccept(mails -> {
                    currentMails.addAll(mails);
                    Bukkit.getScheduler().runTask(plugin, this::populateInventory);
                });
    }

    /**
     * Populates the inventory with mail items and navigation buttons
     */
    private void populateInventory() {
        inventory.clear();

        ConfigurationSection guiConfig = plugin.getConfigManager().getGuiSection("inbox");

        // Add mail items
        int slot = 0;
        for (Mail mail : currentMails) {
            if (slot >= 45) break; // Reserve bottom row for navigation

            ItemStack mailItem = createMailItem(mail);
            inventory.setItem(slot, mailItem);
            slot++;
        }

        addNavigationButtons(guiConfig);
        fillEmptySlots(guiConfig);

        player.openInventory(inventory);
    }

    /**
     * Creates an ItemStack representing a mail message
     *
     * @param mail the mail to represent
     * @return the ItemStack
     */
    private @NotNull ItemStack createMailItem(@NotNull Mail mail) {
        ConfigurationSection mailItemConfig = plugin.getConfigManager()
                .getGuiSection("inbox.mail-item");

        Material material = mail.isRead()
                ? Material.valueOf(mailItemConfig.getString("read-material", "PAPER"))
                : Material.valueOf(mailItemConfig.getString("unread-material", "WRITABLE_BOOK"));

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        Component displayName = Component.text("Mail from " + mail.getSenderName())
                .color(mail.isRead() ? NamedTextColor.GRAY : NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false);
        meta.displayName(displayName);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        String preview = mail.getMessagePreview(30);
        lore.add(Component.text(preview).color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));

        lore.add(Component.empty());
        lore.add(Component.text("Date: " + dateFormat.format(new Date(mail.getTimestamp())))
                .color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));

        if (mail.hasItems()) {
            lore.add(Component.text("📦 Contains items")
                    .color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        }

        if (mail.isRead()) {
            lore.add(Component.text("✓ Read").color(NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(Component.text("✉ Unread").color(NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false));
        }

        lore.add(Component.empty());
        lore.add(Component.text("Click to view").color(NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Adds navigation buttons to the inventory
     *
     * @param guiConfig the GUI configuration section
     */
    private void addNavigationButtons(ConfigurationSection guiConfig) {
        // Previous page button
        if (page > 0) {
            ItemStack prevButton = createButton(
                    Material.valueOf(guiConfig.getString("previous-button.material", "ARROW")),
                    guiConfig.getString("previous-button.name", "Previous Page"),
                    guiConfig.getStringList("previous-button.lore")
            );
            inventory.setItem(guiConfig.getInt("previous-button.slot", 45), prevButton);
        }

        // Next page button
        if (currentMails.size() >= plugin.getConfigManager().getMessagesPerPage()) {
            ItemStack nextButton = createButton(
                    Material.valueOf(guiConfig.getString("next-button.material", "ARROW")),
                    guiConfig.getString("next-button.name", "Next Page"),
                    guiConfig.getStringList("next-button.lore")
            );
            inventory.setItem(guiConfig.getInt("next-button.slot", 53), nextButton);
        }

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

        meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));

        if (loreList != null && !loreList.isEmpty()) {
            List<Component> lore = new ArrayList<>();
            for (String line : loreList) {
                lore.add(Component.text(line).decoration(TextDecoration.ITALIC, false));
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

        ConfigurationSection guiConfig = plugin.getConfigManager().getGuiSection("inbox");
        int slot = event.getSlot();

        // Handle navigation buttons
        if (slot == guiConfig.getInt("previous-button.slot", 45) && page > 0) {
            clicker.closeInventory();
            new InboxGUI(plugin, player, page - 1).open();
            return;
        }

        if (slot == guiConfig.getInt("next-button.slot", 53)) {
            clicker.closeInventory();
            new InboxGUI(plugin, player, page + 1).open();
            return;
        }

        if (slot == guiConfig.getInt("close-button.slot", 49)) {
            clicker.closeInventory();
            return;
        }

        // Handle mail item click
        if (slot < currentMails.size()) {
            Mail mail = currentMails.get(slot);
            clicker.closeInventory();
            new MailDetailGUI(plugin, player, mail, this).open();
        }
    }

    @EventHandler
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            InventoryClickEvent.getHandlerList().unregister(this);
            InventoryCloseEvent.getHandlerList().unregister(this);
        }
    }
}