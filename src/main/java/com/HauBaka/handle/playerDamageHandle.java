package com.HauBaka.handle;

import com.HauBaka.arena.Arena;
import com.HauBaka.event.PlayerDamagePlayerEvent;
import com.HauBaka.player.GamePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class playerDamageHandle implements Listener {
    @EventHandler
    public void onDamage(PlayerDamagePlayerEvent event) {
        GamePlayer victim = event.getVictim();
        GamePlayer attacker = event.getAttacker();
        Arena arena = event.getArena();
        if (!arena.getAlive_players().contains(victim) || !arena.getAlive_players().contains(attacker)) {
            event.getParentEvent().setCancelled(true);
            return;
        }
    }

}
