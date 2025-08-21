package com.HauBaka.handle;

import com.HauBaka.arena.Arena;
import com.HauBaka.enums.ArenaState;
import com.HauBaka.event.BlockBreakEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class blockBreakHandle implements Listener {
    @EventHandler
    public void blockBreakEvent(BlockBreakEvent event) {
        Arena arena = event.getArena();
        if (
                arena.getState() == ArenaState.AVAILABLE ||
                        arena.getState() == ArenaState.WAITING ||
                        arena.getState() == ArenaState.STARTING ||
                        arena.getState() == ArenaState.CAGE_OPENING ||
                        arena.getState() == ArenaState.ENDING ||
                        arena.getState() == ArenaState.CLOSED
        ) event.getParentEvent().setCancelled(true);
    }
}
