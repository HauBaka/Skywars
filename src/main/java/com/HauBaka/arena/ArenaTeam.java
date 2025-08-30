package com.HauBaka.arena;

import com.HauBaka.Skywars;
import com.HauBaka.object.ArenaChest;
import com.HauBaka.object.cage.Cage;
import com.HauBaka.object.cage.CageManager;
import com.HauBaka.player.GamePlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class ArenaTeam {
    @Getter
    private final Location spawnLocation;
    @Getter @Setter
    private List<ArenaChest> spawnChests;
    @Getter
    private final  List<GamePlayer> members;
    @Getter
    private final char name;
    private final Arena arena;
    ArenaTeam(Arena arena, Location spawn, char name) {
        this.arena = arena;
        this.spawnLocation = spawn;
        this.name = name;
        this.spawnChests = new ArrayList<>();
        this.members = new ArrayList<>();
    }

    public boolean addPlayer(GamePlayer player) {
        if (members.contains(player) || members.size() == arena.getVariant().getMode().getPlayerPerTeam()) return false;
        player.getPlayer().teleport(
                Skywars.getConfigConfig().getConfig().getBoolean("waiting_lobby") ?
                        arena.getLobby() : spawnLocation
        );
        members.add(player);
        arena.getCountDownTask().updateScoreboard(player);
        return true;
    }
    public boolean removePlayer(GamePlayer player) {
        if (!members.contains(player)) return false;

        members.remove(player);
        arena.getPlayers().remove(player);

        player.setArena(null);

        return true;
    }
    public int size() {
        return members.size();
    }

    public void refill() {
        for (ArenaChest chest : spawnChests) {
            chest.refill();
        }
    }

    public void joinCage() {
        if (members.isEmpty()) return;
        members.get(0).getPlayerData().getSelectedCage().place(spawnLocation.clone().add(0,-1,0));
        for (GamePlayer member : members) {
            member.getPlayer().teleport(spawnLocation);
            member.getScoreboard().setArena(arena,this);
        }
    }
    public void removeCage() {
        Location loc = spawnLocation.clone().add(0,-1,0);
        Cage cage = members.isEmpty() ? CageManager.getCage("default") : members.get(0).getPlayerData().getSelectedCage();
        if (cage != null) cage.remove(loc);
    }

}
