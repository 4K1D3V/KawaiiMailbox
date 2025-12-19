package dev.oumaimaa.session;

import dev.oumaimaa.model.MailMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages mail composition sessions for players.
 * Provides session lifecycle management and cleanup.
 */
public final class SessionManager {

    private static final Map<UUID, MailSession> SESSIONS = new HashMap<>();

    private SessionManager() {
    }

    /**
     * Creates a new session for a player.
     *
     * @param player the player
     * @param mail   the mail message being composed
     * @return the created session
     */
    public static @NotNull MailSession createSession(Player player, MailMessage mail) {
        MailSession session = new MailSession(player, mail);
        SESSIONS.put(player.getUniqueId(), session);
        return session;
    }

    /**
     * Gets a player's active session.
     *
     * @param player the player
     * @return the session, or null if none exists
     */
    public static @Nullable MailSession getSession(@NotNull Player player) {
        MailSession session = SESSIONS.get(player.getUniqueId());

        // Remove if expired
        if (session != null && session.isExpired()) {
            SESSIONS.remove(player.getUniqueId());
            return null;
        }

        return session;
    }

    /**
     * Removes a player's session.
     *
     * @param player the player
     */
    public static void removeSession(@NotNull Player player) {
        SESSIONS.remove(player.getUniqueId());
    }

    /**
     * Clears all sessions.
     */
    public static void clearAll() {
        SESSIONS.clear();
    }

    /**
     * Cleans up expired sessions.
     */
    public static void cleanupExpired() {
        SESSIONS.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
