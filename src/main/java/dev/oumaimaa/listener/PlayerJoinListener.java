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

import java.util.concurrent.TimeUnit;

/**
 * Listens for player join events to notify about unread mail.
 * Provides comprehensive visual and audio notifications when players have new messages.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Delayed mail checking to avoid login spam</li>
 *   <li>Configurable sound notifications</li>
 *   <li>Optional particle effects</li>
 *   <li>Optional firework displays</li>
 *   <li>Auto-open inbox feature</li>
 *   <li>Clickable inbox prompt</li>
 * </ul>
 *
 * @author Oumaimaa
 * @version 1.0.0
 * @since 1.0.0
 */
public final class PlayerJoinListener {

    private final Main plugin = Main.getInstance();
    private final MailRepository repository = new MailRepository(plugin);
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    /**
     * Creates and registers the event listener.
     *
     * @return the configured event listener
     */
    public static EventListener<PlayerJoinEvent> create() {
        PlayerJoinListener listener = new PlayerJoinListener();
        return EventListener.of(PlayerJoinEvent.class)
                .handler(listener::handle)
                .build();
    }

    /**
     * Handles the player join event with proper error handling.
     *
     * @param event the join event
     */
    private void handle(@NotNull PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        // Verify MongoDB availability
        if (!plugin.getMongoManager().isConnected()) {
            plugin.getLogger().fine("Skipping mail check for " + player.getName() + " - MongoDB unavailable");
            return;
        }

        // Schedule delayed mail check (1 second after join)
        Task.builder()
                .delay(TimeUnit.SECONDS.toMillis(1))
                .whenRan(task -> {
                    if (player.isOnline()) {
                        checkForNewMail(player);
                    }
                })
                .run();
    }

    /**
     * Checks for new mail and notifies the player asynchronously.
     *
     * @param player the player to check
     */
    private void checkForNewMail(@NotNull Player player) {
        // Perform database check asynchronously
        Task.builder()
                .async()
                .whenRan(task -> {
                    try {
                        final long unreadCount = repository.getUnreadCount(player.getUniqueId());

                        if (unreadCount > 0) {
                            // Schedule notification on main thread
                            Task.builder()
                                    .delay(0L)
                                    .whenRan(syncTask -> {
                                        if (player.isOnline()) {
                                            sendNotification(player, unreadCount);
                                        }
                                    })
                                    .run();
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error checking mail for " + player.getName() + ": " + e.getMessage());
                        if (plugin.getLogger().isLoggable(java.util.logging.Level.FINE)) {
                            e.printStackTrace();
                        }
                    }
                })
                .run();
    }

    /**
     * Sends comprehensive notification to player about new mail.
     *
     * @param player the player
     * @param count  the number of unread messages
     */
    private void sendNotification(@NotNull Player player, long count) {
        try {
            // Send chat notification with clickable link
            final Component notification = miniMessage.deserialize(
                    plugin.getMessagesConfig().getNotificationNewMail()
                            .replace("{count}", String.valueOf(count))
            );

            final Component clickPrompt = miniMessage.deserialize(
                    plugin.getMessagesConfig().getNotificationClickToOpen()
            ).clickEvent(ClickEvent.runCommand("/mail inbox"));

            player.sendMessage(Component.empty());
            player.sendMessage(notification);
            player.sendMessage(clickPrompt);
            player.sendMessage(Component.empty());

            // Play notification sound
            playNotificationSound(player);

            // Show particle effects if enabled
            if (plugin.getMainConfig().getEnableParticles()) {
                showParticles(player);
            }

            // Launch fireworks if enabled
            if (plugin.getMainConfig().getEnableFireworks()) {
                launchFirework(player);
            }

            // Auto-open inbox if enabled
            if (plugin.getMainConfig().getAutoOpenInboxOnJoin()) {
                scheduleInboxAutoOpen(player);
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Error sending notification to " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Plays notification sound to the player with error handling.
     *
     * @param player the player
     */
    private void playNotificationSound(@NotNull Player player) {
        try {
            final String soundName = plugin.getMainConfig().getNotificationSound();
            final Sound sound = Sound.valueOf(soundName);

            player.playSound(
                    player.getLocation(),
                    sound,
                    plugin.getMainConfig().getNotificationSoundVolume(),
                    plugin.getMainConfig().getNotificationSoundPitch()
            );
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound configured: " + plugin.getMainConfig().getNotificationSound());
        } catch (Exception e) {
            plugin.getLogger().warning("Error playing notification sound: " + e.getMessage());
        }
    }

    /**
     * Shows particle effects around the player with error handling.
     *
     * @param player the player
     */
    private void showParticles(@NotNull Player player) {
        try {
            final String particleName = plugin.getMainConfig().getParticleType();
            final Particle particle = Particle.valueOf(particleName);
            final Location loc = player.getLocation().add(0, 2, 0);

            player.getWorld().spawnParticle(
                    particle,
                    loc,
                    plugin.getMainConfig().getParticleCount(),
                    0.5, 0.5, 0.5,
                    0.1
            );
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid particle type configured: " + plugin.getMainConfig().getParticleType());
        } catch (Exception e) {
            plugin.getLogger().warning("Error spawning particles: " + e.getMessage());
        }
    }

    /**
     * Launches a firework at the player's location with error handling.
     *
     * @param player the player
     */
    private void launchFirework(@NotNull Player player) {
        try {
            final Location loc = player.getLocation();
            final Firework firework = player.getWorld().spawn(loc, Firework.class);

            final FireworkEffect effect = FireworkEffect.builder()
                    .with(FireworkEffect.Type.BURST)
                    .withColor(Color.FUCHSIA, Color.AQUA)
                    .withFade(Color.WHITE)
                    .flicker(true)
                    .build();

            final FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(effect);
            meta.setPower(0);
            firework.setFireworkMeta(meta);

            // Schedule immediate detonation
            Task.builder()
                    .delay(TimeUnit.MILLISECONDS.toMillis(50))
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

    /**
     * Schedules automatic inbox opening after configured delay.
     *
     * @param player the player
     */
    private void scheduleInboxAutoOpen(@NotNull Player player) {
        final long delayTicks = plugin.getMainConfig().getAutoOpenDelayTicks();

        Task.builder()
                .delay(TimeUnit.MILLISECONDS.toMillis(delayTicks * 50)) // Convert ticks to milliseconds
                .whenRan(task -> {
                    if (player.isOnline() && !player.isDead()) {
                        try {
                            new InboxGui(player, 0).open();
                        } catch (Exception e) {
                            plugin.getLogger().warning("Error auto-opening inbox for " + player.getName() + ": " + e.getMessage());
                        }
                    }
                })
                .run();
    }
}