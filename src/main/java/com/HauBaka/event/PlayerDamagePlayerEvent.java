package com.HauBaka.event;

import com.HauBaka.arena.Arena;
import com.HauBaka.player.GamePlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PlayerDamagePlayerEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    private final Arena arena;
    @Getter
    private final GamePlayer victim;
    @Getter
    private final GamePlayer attacker;
    @Getter
    private final EntityDamageByEntityEvent parentEvent;
    public PlayerDamagePlayerEvent(Arena arena, GamePlayer victim, GamePlayer attacker, EntityDamageByEntityEvent parentEvent) {
        this.arena = arena;
        this.victim = victim;
        this.attacker = attacker;
        this.parentEvent = parentEvent;
    }
    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean b) {

    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
