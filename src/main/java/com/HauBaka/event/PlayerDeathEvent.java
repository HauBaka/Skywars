package com.HauBaka.event;

import com.HauBaka.arena.Arena;
import com.HauBaka.player.GamePlayer;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDeathEvent;


public class PlayerDeathEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    private final Arena arena;
    @Getter
    private final GamePlayer victim;
    @Getter
    private final GamePlayer attacker;
    @Getter
    private final org.bukkit.event.entity.PlayerDeathEvent  parentEvent;
    public PlayerDeathEvent(Arena arena, GamePlayer victim, GamePlayer attacker, org.bukkit.event.entity.PlayerDeathEvent parentEvent) {
        this.arena = arena;
        this.victim = victim;
        this.attacker = attacker;
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
