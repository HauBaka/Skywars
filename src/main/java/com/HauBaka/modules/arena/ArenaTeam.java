package com.HauBaka.modules.arena;

import com.HauBaka.modules.Utils;
import com.HauBaka.modules.arena.enums.ArenaState;
import com.HauBaka.modules.arena.object.ArenaChest;
import com.HauBaka.modules.arena.object.ChestItem;
import com.HauBaka.modules.player.GamePlayer;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

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

        members.add(player);
        player.getPlayer().teleport(spawnLocation);
        arena.broadcast(player.getPlayer().getName() + " joined the game!");

        if (arena.getPlayers().size() == arena.getVariant().getMode().getMinPlayer() && arena.getState() == ArenaState.WAITING) {
            arena.setState(ArenaState.STARTING);
        }
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


}
