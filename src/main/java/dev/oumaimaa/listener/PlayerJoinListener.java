package dev.oumaimaa.listener;

import dev.oumaimaa.Main;
import dev.oumaimaa.gui.InboxGui;
import dev.oumaimaa.repository.MailRepository;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;
import org.slimecraft.bedrock.event.EventListener;
import org.slimecraft.bedrock.task.Task;

/**
 * Listens for player join events to notify about unread mail.
 * Provides visual and audio notifications when players have new messages.
 */
public final class PlayerJoinListener {

    private final Main plugin = Main.getInstance();
    private final MailRepository repository = new MailRepository(plugin);
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    /**
     * Creates and registers the event listener.
     *
     * @return the event listener builder
     */
    public static EventListener<PlayerJoinEvent> create() {
        PlayerJoinListener listener = new PlayerJoinListener();
        return EventListener.of(PlayerJoinEvent.class)
                .handler(listener::handle)
                .build();
    }

    /**
     * Handles the player join event.
     *
     * @param event the join event
     */
    private void handle(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Check MongoDB availability
        if (!plugin.getMongoManager().isConnected()) {
            return;
        }

        // Delay check to avoid spam on join
        Task.builder()
                .delay(20L) // 1-second delay
                .whenRan(task -> checkForNewMail(player))
                .run();
    }

    /**
     * Checks for new mail and notifies the player.
     *
     * @param player the player to check
     */
    private void checkForNewMail(Player player) {
        try {
            long unreadCount = repository.getUnreadCount(player.getUniqueId());

            if (unreadCount > 0) {
                sendNotification(player, unreadCount);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error checking mail for " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Sends notification to player about new mail.
     *
     * @param player the player
     * @param count  the number of unread messages
     */
    private void sendNotification(Player player, long count) {
        // Send chat notification
        Component notification = miniMessage.deserialize(
                plugin.getMessagesConfig().getNotificationNewMail()
                        .replace("{count}", String.valueOf(count))
        );

        Component clickPrompt = miniMessage.deserialize(
                plugin.getMessagesConfig().getNotificationClickToOpen()
        ).clickEvent(ClickEvent.runCommand("/mail inbox"));

        player.sendMessage(notification);
        player.sendMessage(clickPrompt);

        // Play sound
        playNotificationSound(player);

        // Show particles
        if (plugin.getMainConfig().getEnableParticles()) {
            showParticles(player);
        }

        // Launch fireworks
        if (plugin.getMainConfig().getEnableFireworks()) {
            launchFirework(player);
        }

        // Auto-open inbox if enabled
        if (plugin.getMainConfig().getAutoOpenInboxOnJoin()) {
            Task.builder()
                    .delay(plugin.getMainConfig().getAutoOpenDelayTicks())
                    .whenRan(task -> {
                        if (player.isOnline()) {
                            new InboxGui(player, 0).open();
                        }
                    })
                    .run();
        }
    }

    /**
     * Plays notification sound to the player.
     *
     * @param player the player
     */
    private void playNotificationSound(@NotNull Player player) {
        try {
            Sound sound = Sound.valueOf(plugin.getMainConfig().getNotificationSound());
            player.playSound(
                    player.getLocation(),
                    sound,
                    plugin.getMainConfig().getNotificationSoundVolume(),
                    plugin.getMainConfig().getNotificationSoundPitch()
            );
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound: " + plugin.getMainConfig().getNotificationSound());
        }
    }

    /**
     * Shows particles around the player.
     *
     * @param player the player
     */
    private void showParticles(@NotNull Player player) {
        try {
            Particle particle = Particle.valueOf(plugin.getMainConfig().getParticleType());
            Location loc = player.getLocation().add(0, 2, 0);

            player.getWorld().spawnParticle(
                    particle,
                    loc,
                    plugin.getMainConfig().getParticleCount(),
                    0.5, 0.5, 0.5,
                    0.1
            );
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid particle type: " + plugin.getMainConfig().getParticleType());
        }
    }

    /**
     * Launches a firework at the player's location.
     *
     * @param player the player
     */
    private void launchFirework(Player player) {
        try {
            Location loc = player.getLocation();
            Firework firework = player.getWorld().spawn(loc, Firework.class);

            FireworkEffect effect = FireworkEffect.builder()
                    .with(FireworkEffect.Type.BURST)
                    .withColor(Color.FUCHSIA, Color.AQUA)
                    .withFade(Color.WHITE)
                    .flicker(true)
                    .build();

            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(effect);
            meta.setPower(0);
            firework.setFireworkMeta(meta);

            // Detonate immediately
            Task.builder()
                    .delay(1L)
                    .whenRan(task -> {
                        if (firework.isValid()) {
                            firework.detonate();
                        }
                    })
                    .run();

        } catch (Exception e) {
            plugin.getLogger().warning("Error launching firework: " + e.getMessage());
        }
    }
}