package dev.oumaimaa.listeners;

import dev.oumaimaa.Main;
import dev.oumaimaa.gui.InboxGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Listens for player connection events and handles mail notifications
 *
 * <p>This class manages the join experience, checking for unread mail
 * and notifying players with messages, sounds, and optional auto-open inbox.</p>
 *
 * @author Oumaimaa
 * @version 1.0.0
 */
public class PlayerConnectionListener implements Listener {

    private final Main plugin;
    private final Set<UUID> processingPlayers;

    /**
     * Constructs a new player connection listener
     *
     * @param plugin the main plugin instance
     */
    public PlayerConnectionListener(Main plugin) {
        this.plugin = plugin;
        this.processingPlayers = new HashSet<>();
    }

    /**
     * Handles player join events and checks for unread mail
     *
     * @param event the player join event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // Prevent duplicate processing for rapid join/quit scenarios
        if (processingPlayers.contains(playerUUID)) {
            return;
        }

        processingPlayers.add(playerUUID);

        // Check for unread mail asynchronously with a small delay to avoid login spam
        Bukkit.getScheduler().runTaskLater(plugin, () -> checkUnreadMail(player), 20L); // 1-second delay after join
    }

    /**
     * Checks for unread mail and notifies the player
     *
     * @param player the player to check
     */
    private void checkUnreadMail(@NotNull Player player) {
        if (!player.isOnline()) {
            processingPlayers.remove(player.getUniqueId());
            return;
        }

        // Graceful handling if MongoDB is not connected
        if (!plugin.getMongoDBManager().isConnected()) {
            plugin.getLogger().warning("Cannot check unread mail for " + player.getName()
                    + " - MongoDB not connected");
            processingPlayers.remove(player.getUniqueId());
            return;
        }

        plugin.getMailboxManager().countUnreadMail(player.getUniqueId())
                .thenAccept(unreadCount -> {
                    if (!player.isOnline()) {
                        processingPlayers.remove(player.getUniqueId());
                        return;
                    }

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        try {
                            if (unreadCount > 0) {
                                notifyPlayer(player, unreadCount);
                            }
                        } finally {
                            processingPlayers.remove(player.getUniqueId());
                        }
                    });
                })
                .exceptionally(throwable -> {
                    plugin.getLogger().severe("Failed to check unread mail for " + player.getName()
                            + ": " + throwable.getMessage());
                    processingPlayers.remove(player.getUniqueId());
                    return null;
                });
    }

    /**
     * Notifies the player about unread mail with visual and audio effects
     *
     * @param player the player to notify
     * @param unreadCount the number of unread messages
     */
    private void notifyPlayer(@NotNull Player player, long unreadCount) {
        if (!player.isOnline()) {
            return;
        }

        // Send chat notification with clickable link
        Component message = plugin.getConfigManager()
                .getMessage("unread-mail-notification",
                        "{count}", String.valueOf(unreadCount))
                .append(Component.text(" "))
                .append(Component.text("[Click to open]")
                        .color(NamedTextColor.AQUA)
                        .clickEvent(ClickEvent.runCommand("/mail inbox")));

        player.sendMessage(message);

        // Play sound notification
        String soundName = plugin.getConfigManager()
                .getString("notifications.sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
        float volume = (float) plugin.getConfigManager()
                .getConfig().getDouble("notifications.sound-volume", 1.0);
        float pitch = (float) plugin.getConfigManager()
                .getConfig().getDouble("notifications.sound-pitch", 1.0);

        Sound sound = Registry.SOUND_EVENT.get(Objects.requireNonNull(NamespacedKey.fromString(soundName.toLowerCase())));
        if (sound != null) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        } else {
            plugin.getLogger().warning("Invalid sound configured: " + soundName + " (resolved to null)");
        }

        // Show particles if enabled
        if (plugin.getConfigManager().getBoolean("notifications.particles-enabled", true)) {
            String particleName = plugin.getConfigManager()
                    .getString("notifications.particle-type", "VILLAGER_HAPPY");
            int particleCount = plugin.getConfigManager()
                    .getInt("notifications.particle-count", 10);

            Particle particle = Registry.PARTICLE_TYPE.get(Objects.requireNonNull(NamespacedKey.fromString(particleName.toLowerCase())));
            if (particle != null) {
                player.spawnParticle(particle, player.getLocation().add(0, 2, 0),
                        particleCount, 0.5, 0.5, 0.5, 0.1);
            } else {
                plugin.getLogger().warning("Invalid particle configured: " + particleName + " (resolved to null)");
            }
        }

        // Auto-open inbox if enabled
        if (plugin.getConfigManager().isInboxAutoOpenEnabled()) {
            long delay = plugin.getConfigManager().getInboxAutoOpenDelay();

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && !player.isDead()) {
                    new InboxGUI(plugin, player, 0).open();
                }
            }, delay);
        }
    }
}