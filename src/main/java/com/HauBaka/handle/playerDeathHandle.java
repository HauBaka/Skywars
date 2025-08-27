package com.HauBaka.handle;

import com.HauBaka.arena.Arena;
import com.HauBaka.event.PlayerDeathEvent;
import com.HauBaka.player.GamePlayer;
import com.HauBaka.utils.ChatUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class playerDeathHandle implements Listener {
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        GamePlayer victim = event.getVictim();
        GamePlayer attacker = event.getAttacker();
        Arena arena = event.getArena();


    }
}
