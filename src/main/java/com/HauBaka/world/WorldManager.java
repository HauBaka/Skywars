package com.HauBaka.world;

import com.HauBaka.Skywars;
import com.HauBaka.utils.Utils;
import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WorldManager  {
    private static SlimePlugin slimePlugin;
    private static SlimeLoader slimeLoader;
    private static SlimeWorld.SlimeProperties slimeProperties;
    @Getter
    private static List<String> createdWorlds;
    public static void init() {
        createdWorlds = new ArrayList<>();
        slimePlugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
        slimeProperties = SlimeWorld.SlimeProperties.builder().difficulty(0).allowAnimals(false).allowMonsters(false).spawnX(0).spawnY(50).spawnZ(0).pvp(false).readOnly(true).build();
        slimeLoader = slimePlugin.getLoader("file");
    }
    public static boolean checkExistWorldName(String name) {
        try {
            return slimeLoader.worldExists(name);
        } catch (Exception e) {
            return false;
        }
    }
    /***
     * TODO: Chưa kiểm tra xem, file .slime tồn tại hay chưa
     * Hàm sẽ tạo ra một thế giới mới - clone dựa trên file .slime
     * @param sourceWorldName - Là chuỗi không chứa đuôi .slime
     * @param newWorldName - Tên của thế giới mới
     * @return
     */
    public static void cloneWorld(String sourceWorldName, String newWorldName, Consumer<World> callback) {
        if (createdWorlds.contains(newWorldName)) {
            Bukkit.getLogger().warning("World " + newWorldName + " already exists");
            callback.accept(null);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(Skywars.getInstance(), () -> {
            try {
                if (!slimeLoader.worldExists(sourceWorldName)) {
                    Bukkit.getLogger().warning("Source world does not exist: " + sourceWorldName);
                    callback.accept(null);
                    return;
                }

                SlimeWorld slimeWorld = slimePlugin.loadWorld(slimeLoader, sourceWorldName, true, new SlimePropertyMap())
                        .clone(newWorldName);

                Bukkit.getScheduler().runTask(Skywars.getInstance(), () -> {
                    try {
                        slimePlugin.generateWorld(slimeWorld);
                        createdWorlds.add(newWorldName);
                        World world = Bukkit.getWorld(newWorldName);
                        callback.accept(world);
                        Bukkit.getLogger().info("Created clone " + sourceWorldName + " to " + newWorldName);
                    } catch (IllegalArgumentException ex) {
                        Bukkit.getLogger().severe("Error generating world: " + ex.getMessage());
                        callback.accept(null);
                    }
                });
            } catch (Exception e) {
                Bukkit.getLogger().severe("Error cloning world: " + e.getMessage());
                e.printStackTrace();
                callback.accept(null);
            }
        });
    }
    public static String cloneWorld(String sourceWorldName, Consumer<World> callback) {
        String worldID = Utils.generateID(5);
        while (createdWorlds.contains(worldID)) worldID=Utils.generateID(5);
        cloneWorld(sourceWorldName, worldID, callback);
        return worldID;
    }
    /***
     * Hàm xóa thế giới đã tạo
     @param worldName : tên của thế giới cần phải xóa
     ***/
    public static void removeWorld(String worldName) {

        World removeW = Bukkit.getWorld(worldName);
        if (worldName == null || removeW == null || !(createdWorlds.contains(worldName))) {
            Bukkit.getLogger().warning("World " + worldName + " does not exist");
            return;
        }
        Bukkit.unloadWorld(removeW, true);
        createdWorlds.remove(worldName);
        Bukkit.getLogger().info("Removed world: " + worldName);
    }

    /***
     * Xóa tất cả các world đã tạo
     */
    public static void removeAllCreatedWorlds() {
        for (String worldName : createdWorlds) {
            removeWorld(worldName);
        }
    }

    public static void disableWorldLogs() {
        Logger logger = Logger.getLogger("SlimeWorldManager");
        logger.setLevel(Level.WARNING);

        logger = Bukkit.getServer().getLogger();
        logger.setFilter(record -> {
            String msg = record.getMessage();
            if (msg.contains("World Settings For")) return false;
            if (msg.contains("Chunks to Grow per Tick")) return false;
            if (msg.contains("Clear tick list")) return false;
            if (msg.contains("Experience Merge Radius")) return false;
            if (msg.contains("View Distance")) return false;
            if (msg.contains("Arrow Despawn Rate")) return false;
            if (msg.contains("Item Despawn Rate")) return false;
            if (msg.contains("Item Merge Radius")) return false;
            if (msg.contains("Allow Zombie Pigmen")) return false;
            if (msg.contains("Zombie Aggressive Towards Villager")) return false;
            if (msg.contains("Max Entity Collisions")) return false;
            if (msg.contains("Custom Map Seeds")) return false;
            if (msg.contains("Tile Max Tick Time")) return false;
            if (msg.contains("Max TNT Explosions")) return false;
            if (msg.contains("Anti X-Ray")) return false;
            if (msg.contains("Engine Mode")) return false;
            if (msg.contains("Hidden Blocks")) return false;
            if (msg.contains("Replace Blocks")) return false;
            if (msg.contains("Hopper Transfer")) return false;
            if (msg.contains("Mob Spawn Range")) return false;
            if (msg.contains("Structure Info Saving")) return false;
            if (msg.contains("Growth Modifier")) return false;
            if (msg.contains("Nerfing mobs spawned from spawners")) return false;
            if (msg.contains("Entity Tracking Range")) return false;
            if (msg.contains("Entity Activation Range")) return false;
            if (msg.contains("Random Lighting Updates")) return false;
            if (msg.contains("Sending up to")) return false;
            if (msg.contains("Skipping BlockEntity with id")) return false;
            if (msg.contains("Loading world")) return false;
            return true;
        });
    }
}
