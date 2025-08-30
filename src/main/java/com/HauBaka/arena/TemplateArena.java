package com.HauBaka.arena;

import com.HauBaka.arena.setup.ArenaSetup;
import com.HauBaka.enums.ArenaSetupStage;
import com.HauBaka.object.TemplateBlock;
import com.HauBaka.utils.Utils;
import com.HauBaka.object.ArenaChest;
import com.HauBaka.file.FileConfig;
import lombok.Getter;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TemplateArena {
    @Getter
    private final String mapName;
    @Getter
    private String name;
    @Getter
    private TemplateBlock lobby;
    @Getter
    private List<TemplateBlock> spawns;
    @Getter
    private List<List<TemplateBlock>> spawnChests;
    @Getter
    private List<TemplateBlock> midChests;
    private final FileConfig fileConfig;
    @Getter
    private boolean isValid;
    public TemplateArena(String mapName) {
        this.mapName = mapName;
        this.name = Utils.toBetterName(mapName);
        fileConfig = new FileConfig("/maps/" + mapName +".yml");
        isValid = fileConfig.getFile().exists();
        load();
    }
    private void load() {
        if (!isValid) return;
        this.name = fileConfig.getConfig().getString("name", Utils.toBetterName(mapName));

        // Load spawns + spawnChests
        this.spawns = new ArrayList<>();
        this.spawnChests = new ArrayList<>();
        if (fileConfig.getConfig().contains("lobby")) {
            lobby = new TemplateBlock(
                    fileConfig.getConfig().getInt("lobby.x"),
                    fileConfig.getConfig().getInt("lobby.y"),
                    fileConfig.getConfig().getInt("lobby.z"));
        }
        List<Map<?, ?>> spawnsList = fileConfig.getConfig().getMapList("spawns");
        for (Map<?, ?> spawnMap : spawnsList) {
            TemplateBlock spawnLoc = mapToTemplateBlock(spawnMap);
            spawns.add(spawnLoc);

            // Load chests for this spawn
            List<TemplateBlock> chestLocations = new ArrayList<>();
            List<Map<?, ?>> chestList = (List<Map<?, ?>>) spawnMap.get("chests");
            if (chestList != null) {
                for (Map<?, ?> chestMap : chestList) {
                    chestLocations.add(mapToTemplateBlock(chestMap));
                }
            }
            spawnChests.add(chestLocations);
        }

        // Load midChests
        this.midChests = new ArrayList<>();
        List<Map<?, ?>> midChestList = fileConfig.getConfig().getMapList("midChests");
        for (Map<?, ?> chestMap : midChestList) {
            midChests.add(mapToTemplateBlock(chestMap));
        }
    }

    private TemplateBlock mapToTemplateBlock(Map<?, ?> map) {
        int x = ((Number) map.get("x")).intValue();
        int y = ((Number) map.get("y")).intValue();
        int z = ((Number) map.get("z")).intValue();

        float yaw = map.containsKey("yaw") ? ((Number) map.get("yaw")).floatValue() : 0f;
        TemplateBlock templateBlock = new TemplateBlock(x, y, z);
        templateBlock.setyaw(yaw);

        return templateBlock;
    }

    public void setUp(Arena arena) {
        arena.setLobby(arena.getWorld().getBlockAt((int) lobby.getX(), (int) lobby.getY(), (int) lobby.getZ()).getLocation());
        //Add spawn and its chests.
        for (int i = 0 ; i < spawns.size(); ++i) {
            TemplateBlock templateBlock = spawns.get(i);
            Location spawn = new Location(arena.getWorld(),
                    templateBlock.getX(), templateBlock.getY(), templateBlock.getZ(),
                    templateBlock.getDirection().getYaw(), 0f);

            ArenaTeam arenaTeam = new ArenaTeam(arena, spawn, (char) ('A' + i));
            for (TemplateBlock chest : spawnChests.get(i)) {
                Location chestSpawn = new Location(arena.getWorld(),
                        chest.getX(), chest.getY(), chest.getZ());
                ArenaChest arenaChest = new ArenaChest(arena, chestSpawn, ArenaSetupStage.SPAWN);
                arenaTeam.getSpawnChests().add(arenaChest);
            }

            arena.getTeams().add(arenaTeam);
        }
        //Add mid-chests
        for (TemplateBlock chest : midChests) {
            Location chestSpawn = new Location(arena.getWorld(),
                    chest.getX(), chest.getY(), chest.getZ());
            ArenaChest arenaChest = new ArenaChest(arena, chestSpawn, ArenaSetupStage.MID);
            arena.getMidChests().add(arenaChest);
        }
    }
    public void setUp(ArenaSetup arenaSetup) {
        //Add spawn and its chests.
        if (lobby != null)
            arenaSetup.setLobby(arenaSetup.getWorld().getBlockAt(lobby.getX(), lobby.getY(), lobby.getZ()).getLocation());

        for (int i = 0 ; i < spawns.size(); ++i) {
            TemplateBlock spawnTemplate = spawns.get(i);
            Location spawn = new Location(arenaSetup.getWorld(),
                    spawnTemplate.getX(), spawnTemplate.getY(), spawnTemplate.getZ(),
                    spawnTemplate.getDirection().getYaw(), 0f);
            arenaSetup.addTeam(spawn);
            for (TemplateBlock chest : spawnChests.get(i)) {
                Location chestSpawn = new Location(arenaSetup.getWorld(), chest.getX(), chest.getY(), chest.getZ());
                arenaSetup.addChest(chestSpawn.getBlock(), ArenaSetupStage.SPAWN);
            }
        }
        //Add mid-chests
        for (TemplateBlock chest : midChests) {
            Location chestSpawn = new Location(arenaSetup.getWorld(), chest.getX(), chest.getY(), chest.getZ());
            arenaSetup.addChest(chestSpawn.getBlock(), ArenaSetupStage.MID);
        }
    }
}
