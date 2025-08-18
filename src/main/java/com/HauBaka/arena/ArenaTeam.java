package com.HauBaka.arena;

import com.HauBaka.Skywars;
import com.HauBaka.enums.ArenaState;
import com.HauBaka.object.ArenaChest;
import com.HauBaka.object.cage.CageManager;
import com.HauBaka.player.GamePlayer;
import lombok.Getter;
import org.bukkit.Location;

import java.util.List;

public class ArenaTeam {
    @Getter
    private final Location spawnLocation;
    @Getter
    private List<ArenaChest> spawnChests;
    @Getter
    private List<GamePlayer> members;
    @Getter
    private final String name;
    private final Arena arena;
    ArenaTeam(Arena arena, Location spawn, String name) {
        this.arena = arena;
        this.spawnLocation = spawn;
        this.name = name;
    }

    //TODO: implement here!
    public boolean addPlayer(GamePlayer player) {
        if (members.contains(player) || members.size() == arena.getVariant().getMode().getPlayerPerTeam()) return false;
        if (Skywars.getInstance().getConfig().getConfig().getBoolean("waiting_lobby")) {
            if (arena.getLobby() == null) return false;
            player.getPlayer().teleport(arena.getLobby());
        }

        members.add(player);
        player.getPlayer().teleport(spawnLocation);
        arena.broadcast(player.getPlayer().getName() + " joined the game!");

        return true;
    }
    public boolean removePlayer(GamePlayer player) {
        if (!members.contains(player)) return false;
        members.remove(player);
        //TODO: implement here!
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

    public void addCage() {
        if (members.isEmpty()) return;
        members.get(0).getSelectedCage().place(spawnLocation);
        for (GamePlayer member : members) {
            member.getPlayer().teleport(spawnLocation);
        }
    }
    public void removeCage() {
        if (members.isEmpty()) CageManager.getCage("default").remove(spawnLocation);
        members.get(0).getSelectedCage().remove(spawnLocation);
    }

}
