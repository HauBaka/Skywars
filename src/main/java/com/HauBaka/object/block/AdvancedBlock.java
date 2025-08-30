package com.HauBaka.object.block;

import com.HauBaka.Skywars;
import com.HauBaka.arena.TemplateArena;
import com.HauBaka.enums.Direction;
import com.HauBaka.object.Hologram;
import com.HauBaka.object.TemplateBlock;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class AdvancedBlock implements Listener {
    private static final Map<World, List<AdvancedBlock>> listBlocks = new HashMap<>();
    private final int x;
    private final int y;
    private final int z;
    protected final Hologram hologram;
    protected Direction direction;
    private final Map<Action, Runnable> runnables;
    private final Location location;
    public AdvancedBlock(Location loc) {
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
        this.location = loc;
        this.direction = Direction.fromYaw(loc.getYaw());
        this.hologram = new Hologram(loc.clone().add(0,0.75,0));
        this.runnables = new HashMap<>();
        if (!listBlocks.containsKey(loc.getWorld())) listBlocks.put(loc.getWorld(), new ArrayList<>());
        listBlocks.get(loc.getWorld()).add(this);

        Bukkit.getPluginManager().registerEvents(this, Skywars.getInstance());
    }
    public void setyaw(float yaw) {
        direction = Direction.fromYaw(yaw);
    }
    public void addYaw() {
        addYaw(90f);
    }
    public void addYaw(float yaw) {
        direction = Direction.fromYaw(this.direction.getYaw() + yaw);
    }
    public boolean equal(Location loc) {
        if (location == null || loc == null) return false;
        return location.getWorld() == loc.getWorld() &&
                location.getBlockX() == loc.getBlockX()
                && location.getBlockY() == loc.getBlockY()
                && location.getBlockZ() == loc.getBlockZ();
    }
    public void setRunnable(List<Action> actions, Runnable runnable) {
        for (Action action : actions)
            runnables.put(action, runnable);
    }
    public void setRunnable(Action action, Runnable runnable) {
        runnables.put(action, runnable);
    }
    @EventHandler
    public void interact(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block != null &&
                equal(block.getLocation()) &&
                runnables.containsKey(event.getAction())) {
            event.setCancelled(true);
            runnables.get(event.getAction()).run();
        }
    }
    public void destroy() {
        hologram.destroy();
        HandlerList.unregisterAll(this);
    }
    public static void removeWorld(World world) {
        if (!listBlocks.containsKey(world)) return;
        for (AdvancedBlock advancedBlock : listBlocks.get(world)) advancedBlock.destroy();
        listBlocks.remove(world);
    }
}
