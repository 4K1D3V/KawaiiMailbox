package dev.oumaimaa.session;

import dev.oumaimaa.model.MailMessage;
import org.bukkit.entity.Player;

/**
 * Represents a mail composition session for a player.
 * Stores temporary data while a player is composing a mail message.
 */
public final class MailSession {

    private final Player player;
    private final MailMessage mail;
    private final long createdAt;

    public MailSession(Player player, MailMessage mail) {
        this.player = player;
        this.mail = mail;
        this.createdAt = System.currentTimeMillis();
    }

    public Player getPlayer() {
        return player;
    }

    public MailMessage getMail() {
        return mail;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * Checks if this session has expired (older than 5 minutes).
     *
     * @return true if expired, false otherwise
     */
    public boolean isExpired() {
        return System.currentTimeMillis() - createdAt > 300000; // 5 minutes
    }
}