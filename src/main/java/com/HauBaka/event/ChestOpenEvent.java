package com.HauBaka.event;

import com.HauBaka.arena.Arena;
import com.HauBaka.object.ArenaChest;
import com.HauBaka.player.GamePlayer;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ChestOpenEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    private final Arena arena;
    @Getter
    private final GamePlayer gamePlayer;
    @Getter
    private final ArenaChest arenaChest;

    public ChestOpenEvent(Arena arena, GamePlayer gamePlayer, ArenaChest arenaChest) {
        this.arena = arena;
        this.gamePlayer = gamePlayer;
        this.arenaChest = arenaChest;
    }


    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
