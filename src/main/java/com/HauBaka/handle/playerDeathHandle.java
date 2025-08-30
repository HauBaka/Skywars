package com.HauBaka.handle;

import com.HauBaka.arena.Arena;
import com.HauBaka.enums.PlaceholderVariable;
import com.HauBaka.event.PlayerDeathEvent;
import com.HauBaka.player.GamePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class playerDeathHandle implements Listener {
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        GamePlayer victim = event.getVictim();
        GamePlayer attacker = event.getAttacker();
        Arena arena = event.getArena();

        //update scoreboard
        int playersLeft = arena.getAlive_players().size();
        for (GamePlayer player : arena.getAlive_players())
            arena.getCountDownTask().updateScoreboard(player, PlaceholderVariable.PLAYERS_LEFT, playersLeft);
        for (GamePlayer player : arena.getSpectators())
            arena.getCountDownTask().updateScoreboard(player, PlaceholderVariable.PLAYERS_LEFT, playersLeft);
        //broadcast
        if (attacker == null) arena.broadcast(victim.getPlayer().getDisplayName() + " &edied!");

    }
}
