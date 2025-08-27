package com.mjanglin.votingmatters.events;

import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.mjanglin.votingmatters.models.PlayerVoteData;

/**
 * Event fired when a player vote is detected
 */
public class PlayerVoteEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID playerUuid;
    private final String siteName;
    private final PlayerVoteData playerData;
    private boolean cancelled = false;

    public PlayerVoteEvent(UUID playerUuid, String siteName, PlayerVoteData playerData) {
        this.playerUuid = playerUuid;
        this.siteName = siteName;
        this.playerData = playerData;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getSiteName() {
        return siteName;
    }

    public PlayerVoteData getPlayerData() {
        return playerData;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return PlayerVoteEvent.HANDLERS;
    }
}
