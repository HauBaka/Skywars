package com.HauBaka.event;

import com.HauBaka.arena.Arena;
import com.HauBaka.player.GamePlayer;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerInteractEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    private final Arena arena;
    @Getter
    private final GamePlayer player;
    @Getter
    private final org.bukkit.event.player.PlayerInteractEvent  parentEvent;

    public PlayerInteractEvent(Arena arena, GamePlayer player, org.bukkit.event.player.PlayerInteractEvent parentEvent) {
        this.arena = arena;
        this.player = player;
        this.parentEvent = parentEvent;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
