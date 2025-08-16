package com.HauBaka.object;

import com.HauBaka.Skywars;
import com.HauBaka.arena.TemplateArena;
import com.HauBaka.arena.setup.ArenaSetup;
import com.HauBaka.enums.Direction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class TemplateBlock implements Listener {
    private static final Map<World, List<TemplateBlock>> listBlocks = new HashMap<>();
    @Getter
    private final int x;
    @Getter
    private final int y;
    @Getter
    private final int z;
    @Getter
    private final Hologram hologram;
    @Getter
    Direction direction;
    @Getter
    private int teamNumber;

    Map<Action, Consumer<Object>> consumers;
    @Getter @Setter
    private Material oldMaterial;
    private final Location loc;
    @Getter
    private final TemplateArena.TemplateLocation templateLocation;
    public TemplateBlock(Location loc, int teamNumber) {
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
        this.teamNumber = teamNumber;
        this.loc = loc;
        this.direction = Direction.fromYaw(loc.getYaw());
        this.hologram = new Hologram(loc.clone().add(0,0.75,0));
        this.oldMaterial = Material.AIR;
        this.consumers = new HashMap<>();
        this.templateLocation = new TemplateArena.TemplateLocation(x,y,z,direction.getYaw(),0f);
        if (!listBlocks.containsKey(loc.getWorld())) listBlocks.put(loc.getWorld(), new ArrayList<>());
        listBlocks.get(loc.getWorld()).add(this);

        Bukkit.getPluginManager().registerEvents(this, Skywars.getInstance());
    }
    public void addYaw() {
        Direction newDirection = Direction.fromYaw(this.direction.getYaw() + (float) 90.0);
        setDirection(newDirection);
    }
    public void setConsumer(List<Action> actions, Consumer<Object> consumer) {
        for (Action action : actions)
            consumers.put(action, consumer);
    }
    public void setConsumer(Action action, Consumer<Object> consumer) {
        consumers.put(action, consumer);
    }
    public void setDirection(Direction direction) {
        this.direction = direction;
        hologram.setLine(3, "&eDirection: &a" + direction.toString());
    }
    public void setTeam(int teamNumber) {
        if (teamNumber == this.teamNumber) return;
        this.teamNumber = teamNumber;
        hologram.setLine(0, "&aTeam " + teamNumber +"'s spawn");
    }
    public boolean equals(TemplateBlock templateBlock) {
        return x == templateBlock.getX() &&
                y == templateBlock.getY() &&
                z == templateBlock.getZ();
    }

    public TemplateArena.TemplateLocation toTemplateLocation() {
        return new TemplateArena.TemplateLocation(x,y,z,direction.getYaw(),0f);
    }
    @EventHandler
    public void interact(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block != null && block.getLocation().equals(this.loc)) {
            event.setCancelled(true);
            if (consumers.containsKey(event.getAction()))
                consumers.get(event.getAction()).accept(event);
        }
    }

    public void destroy() {
        hologram.destroy();
        HandlerList.unregisterAll(this);
    }
    public static void removeWorld(World world) {
        if (!listBlocks.containsKey(world)) return;
        for (TemplateBlock templateBlock : listBlocks.get(world)) templateBlock.destroy();
        listBlocks.remove(world);
    }
}