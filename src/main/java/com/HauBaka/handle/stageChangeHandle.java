package com.HauBaka.handle;

import com.HauBaka.Skywars;
import com.HauBaka.arena.Arena;
import com.HauBaka.arena.ArenaTeam;
import com.HauBaka.enums.ArenaState;
import com.HauBaka.event.ArenaStageChangeEvent;
import com.HauBaka.player.GamePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class stageChangeHandle implements Listener {
    @EventHandler
    public void onStageChange(ArenaStageChangeEvent event) {
        Arena arena = event.getArena();
        switch (arena.getState()) {
            case STARTING:
                arena.getCountDownTask().starting();
                break;
            case CAGE_OPENING:
                arena.getAlive_players().addAll(arena.getPlayers());
                arena.getAlive_teams().addAll(arena.getTeams());
                for (GamePlayer gamePlayer : arena.getPlayers()) {
                    arena.getKills().put(gamePlayer, 0);
                    arena.getAssists().put(gamePlayer, 0);
                }
                arena.getCountDownTask().cage_opening();
                break;
            case PHASE_1:
                arena.refill();
                for (ArenaTeam team : arena.getTeams())
                    team.removeCage();
                arena.getCountDownTask().doTimerLoop(() -> {});
                break;
            case PHASE_2:
            case PHASE_3:
                arena.refill();
                arena.getCountDownTask().doTimerLoop(() -> {});
                break;
            case DOOM:
                arena.refill();
                arena.getCountDownTask().doTimerLoop(() -> {});
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (arena.getState() == ArenaState.DOOM) spawnDragon(arena);
                        else cancel();
                    }
                }.runTaskTimer(Skywars.getInstance(), 3*20L, 30 * 20L);
                break;
            case ENDING:
                printStats(arena);
                break;
            case CLOSED:
                arena.destroy();
                break;
        }
    }
    private void spawnDragon(Arena arena) {
        arena.broadcast("&e+1 Dragon");
        Entity dragon= arena.getWorld().spawnEntity(arena.getLobby(), EntityType.ENDER_DRAGON);
        dragon.setCustomName("&4&lDragon");
        dragon.setCustomNameVisible(true);
    }
    private void printStats(Arena arena) {
        StringBuilder winner = new StringBuilder();
        for (ArenaTeam team : arena.getAlive_teams()) {
            for (GamePlayer player : team.getMembers())
                if (arena.getAlive_players().contains(player)) {
                    winner.append(player.getPlayer().getDisplayName()).append(", ");
                }
        }
        arena.broadcast("WINNER: " + (winner.length() >= 2 ? winner.substring(0, winner.length() - 2) : "Draw"));

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
