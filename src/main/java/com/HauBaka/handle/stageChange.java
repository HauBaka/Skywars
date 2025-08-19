package com.HauBaka.handle;

import com.HauBaka.arena.Arena;
import com.HauBaka.arena.ArenaTeam;
import com.HauBaka.enums.ArenaState;
import com.HauBaka.event.ArenaStageChangeEvent;
import com.HauBaka.player.GamePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class stageChange implements Listener {
    @EventHandler
    public void onStageChange(ArenaStageChangeEvent event) {
        Arena arena = event.getArena();
        if (event.getNewState() == ArenaState.ENDING) {
            printStats(arena);
        }
    }
    private void printStats(Arena arena) {
        StringBuilder winner = new StringBuilder();
        for (ArenaTeam team : arena.getAlive_teams()) {
            for (GamePlayer player : team.getMembers())
                if (arena.getAlive_players().contains(player)) {
                    winner.append(player.getPlayer().getDisplayName()).append(", ");
                }
        }
        arena.broadcast("WINNER: " + winner.substring(0, winner.length() - 2));

        List<GamePlayer> topKills = getTopPlayers(arena, 3);
        for (int i = 0;i < topKills.size(); ++i) {
            GamePlayer player = topKills.get(i);
            arena.broadcast("#" + (i+1) +" kills: " + (player == null ?
                    "&7None - 0" : player.getPlayer().getDisplayName() +" &7- " + arena.getKills().get(player)));
        }
    }
    public List<GamePlayer> getTopPlayers(Arena arena, int limit) {
        return arena.getKills().entrySet()
                .stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }


}
