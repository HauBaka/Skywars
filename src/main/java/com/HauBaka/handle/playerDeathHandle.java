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
        event.getParentEvent().setDeathMessage(null);
        arena.addSpectator(victim);
        ChatUtils.sendComplexMessage(
                victim.getPlayer(),
                ChatUtils.simple("&cYou died!&e Want to play again? "),
                ChatUtils.command(
                        "&b&lClick here!",
                        "/play " + arena.getVariant().getType().name() + "_" + arena.getVariant().getMode().name(),
                        "Click here to play another game of &bSkywars"
                )
        );
        arena.broadcast(victim.getPlayer().getDisplayName() +" died!");
    }
}
