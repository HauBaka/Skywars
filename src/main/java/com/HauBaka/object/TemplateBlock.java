package com.HauBaka.object;

import com.HauBaka.Skywars;
import com.HauBaka.arena.TemplateArena;
import com.HauBaka.enums.Direction;
import com.HauBaka.object.block.AdvancedBlock;
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
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class TemplateBlock extends AdvancedBlock {
    private static final Map<World, List<TemplateBlock>> listBlocks = new HashMap<>();
    private int teamNumber;

    @Setter
    private Material oldMaterial;
    public TemplateBlock(int x, int y, int z) {
        this(new Location(null, x,y,z), -1);
    }
    public TemplateBlock(Location loc, int teamNumber) {
        super(loc);
        this.teamNumber = teamNumber;
        this.oldMaterial = Material.AIR;
        if (!listBlocks.containsKey(loc.getWorld())) listBlocks.put(loc.getWorld(), new ArrayList<>());
        listBlocks.get(loc.getWorld()).add(this);

        Bukkit.getPluginManager().registerEvents(this, Skywars.getInstance());
    }
    @Override
    public void addYaw() {
        super.addYaw();
        setDirection();
    }
    public void setDirection() {
        this.hologram.setLine(3, "&eDirection: &a" + direction.toString());
    }
    public void setTeam(int teamNumber) {
        if (teamNumber == this.teamNumber) return;
        this.teamNumber = teamNumber;
        hologram.setLine(0, "&aTeam " + teamNumber +"'s spawn");
    }
    public static void removeWorld(World world) {
        if (!listBlocks.containsKey(world)) return;
        for (TemplateBlock templateBlock : listBlocks.get(world)) templateBlock.destroy();
        listBlocks.remove(world);
    }
}