package com.HauBaka.modules.world;

import com.HauBaka.Skywars;
import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.CorruptedWorldException;
import com.grinderwolf.swm.api.exceptions.NewerFormatException;
import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import com.grinderwolf.swm.api.exceptions.WorldInUseException;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.function.Consumer;

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

    /*
    public World cloneWorld(String sourceWorldName) {
        String newWorldName = ArenaManager.generateID();
        if (createdWorlds.contains(newWorldName)) {
            Bukkit.getLogger().warning("World " + newWorldName + " already exists");
            return null;
        }
        try {
            SlimeLoader loader = slimePlugin.getLoader("file"); // Sử dụng loader phù hợp
            SlimeWorld slimeWorld = slimePlugin.loadWorld(loader, sourceWorldName, true, new SlimePropertyMap())
                    .clone(newWorldName);
            slimePlugin.generateWorld(slimeWorld);
            createdWorlds.add(newWorldName);
            return Bukkit.getWorld(newWorldName);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Lỗi khi tạo thế giới tạm thời: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    } */
    /***
     * Hàm xóa thế giới đã tạo
     @param worldName : tên của thế giới cần phải xóa
     @param returnLocation : Vị trí để dịch chuyển các người chơi trong thế giới 'worldName' đến.
     ***/
    public static void removeWorld(String worldName, Location returnLocation) {

        World removeW = Bukkit.getWorld(worldName);
        if (worldName == null || removeW == null || !(createdWorlds.contains(worldName))) {
            Bukkit.getLogger().warning("World " + worldName + " does not exist");
            return;
        }
        if (returnLocation != null) {
            Bukkit.getScheduler().runTask(Skywars.getInstance(), ()-> {
                for (Player player : removeW.getPlayers()) {
                    player.teleport(returnLocation);
                }
            });
        }
        Bukkit.unloadWorld(removeW, true);
        createdWorlds.remove(worldName);
    }

    /***
     * Xóa tất cả các world đã tạo
     */
    public static void removeAllCreatedWorlds() {
        for (String worldName : createdWorlds) {
            removeWorld(worldName, null);
        }
    }
}
