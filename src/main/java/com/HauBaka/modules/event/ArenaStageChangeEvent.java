package com.HauBaka.modules.event;

import com.HauBaka.modules.arena.Arena;
import com.HauBaka.modules.arena.enums.ArenaState;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class ArenaStageChangeEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Arena arena;
    private final ArenaState oldState;
    private final ArenaState newState;

    public ArenaStageChangeEvent(Arena arena, ArenaState oldState, ArenaState newState) {
        this.arena = arena;
        this.oldState = oldState;
        this.newState = newState;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
