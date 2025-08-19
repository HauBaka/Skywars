package com.HauBaka.arena;

import com.HauBaka.arena.setup.ArenaSetup;
import com.HauBaka.enums.ArenaSetupStage;
import com.HauBaka.utils.Utils;
import com.HauBaka.object.ArenaChest;
import com.HauBaka.file.FileConfig;
import lombok.Getter;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TemplateArena {
    public static class TemplateLocation {
        @Getter
        public double X, Y, Z;
        @Getter
        float Yaw, Pitch;
        public TemplateLocation(double X, double Y, double Z, float Yaw, float Pitch) {
            this.X = X;
            this.Y = Y;
            this.Z=  Z;
            this.Yaw=  Yaw;
            this.Pitch = Pitch;
        }
        public TemplateLocation(Location location) {
            this.X = location.getX();
            this.Y = location.getY();
            this.Z = location.getZ();
            this.Yaw = location.getYaw();
            this.Pitch = location.getPitch();
        }

        public boolean equals(TemplateLocation other) {
            return other != null &&
                    other.getX() == X &&
                    other.getY() == Y &&
                    other.getZ() == Z &&
                    other.getPitch() == Pitch &&
                    other.getYaw() == Yaw;
        }
    }
    @Getter
    private final String mapName;
    @Getter
    private String name;
    @Getter
    private TemplateLocation lobby;
    @Getter
    private List<TemplateLocation> spawns;
    @Getter
    private List<List<TemplateLocation>> spawnChests;
    @Getter
    private List<TemplateLocation> midChests;
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
            lobby = new TemplateLocation(
                    fileConfig.getConfig().getDouble("lobby.x"),
                    fileConfig.getConfig().getDouble("lobby.y"),
                    fileConfig.getConfig().getDouble("lobby.z"),
                    0f, 0f
            );
        }
        List<Map<?, ?>> spawnsList = fileConfig.getConfig().getMapList("spawns");
        for (Map<?, ?> spawnMap : spawnsList) {
            TemplateLocation spawnLoc = mapToTemplateLocation(spawnMap);
            spawns.add(spawnLoc);

            // Load chests for this spawn
            List<TemplateLocation> chestLocations = new ArrayList<>();
            List<Map<?, ?>> chestList = (List<Map<?, ?>>) spawnMap.get("chests");
            if (chestList != null) {
                for (Map<?, ?> chestMap : chestList) {
                    chestLocations.add(mapToTemplateLocation(chestMap));
                }
            }
            spawnChests.add(chestLocations);
        }

        // Load midChests
        this.midChests = new ArrayList<>();
        List<Map<?, ?>> midChestList = fileConfig.getConfig().getMapList("midChests");
        for (Map<?, ?> chestMap : midChestList) {
            midChests.add(mapToTemplateLocation(chestMap));
        }
    }

    private TemplateLocation mapToTemplateLocation(Map<?, ?> map) {
        double x = ((Number) map.get("x")).doubleValue();
        double y = ((Number) map.get("y")).doubleValue();
        double z = ((Number) map.get("z")).doubleValue();
        float yaw = map.containsKey("yaw") ? ((Number) map.get("yaw")).floatValue() : 0f;
        float pitch = map.containsKey("pitch") ? ((Number) map.get("pitch")).floatValue() : 0f;
        return new TemplateLocation(x, y, z, yaw, pitch);
    }

    public void setUp(Arena arena) {
        arena.setLobby(arena.getWorld().getBlockAt((int) lobby.getX(), (int) lobby.getY(), (int) lobby.getZ()).getLocation());
        //Add spawn and its chests.
        for (int i = 0 ; i < spawns.size(); ++i) {
            TemplateLocation templateLocation = spawns.get(i);
            Location spawn = new Location(arena.getWorld(),
                    templateLocation.getX(), templateLocation.getY(), templateLocation.getZ(),
                    templateLocation.getYaw(), templateLocation.getPitch());

            ArenaTeam arenaTeam = new ArenaTeam(arena, spawn, String.valueOf((char)'A' + i));
            for (TemplateLocation chestTemplateLocation : spawnChests.get(i)) {
                Location chestSpawn = new Location(arena.getWorld(),
                        chestTemplateLocation.getX(), chestTemplateLocation.getY(), chestTemplateLocation.getZ());
                ArenaChest arenaChest = new ArenaChest(arena, chestSpawn);
                arenaTeam.getSpawnChests().add(arenaChest);
            }

            arena.getTeams().add(arenaTeam);
        }
        //Add mid-chests
        for (TemplateLocation midTemplateLocation : midChests) {
            Location chestSpawn = new Location(arena.getWorld(),
                    midTemplateLocation.getX(), midTemplateLocation.getY(), midTemplateLocation.getZ());
            ArenaChest arenaChest = new ArenaChest(arena, chestSpawn);
            arena.getMidChests().add(arenaChest);
        }
    }
    public void setUp(ArenaSetup arenaSetup) {
        //Add spawn and its chests.
        if (lobby != null)
            arenaSetup.setLobby(
                    arenaSetup.getWorld().getBlockAt(
                            (int) lobby.getX(),
                            (int) lobby.getY(),
                            (int) lobby.getZ()
                    ).getLocation()
            );
        for (int i = 0 ; i < spawns.size(); ++i) {
            TemplateLocation templateLocation = spawns.get(i);
            Location spawn = new Location(arenaSetup.getWorld(),
                    templateLocation.getX(), templateLocation.getY(), templateLocation.getZ(),
                    templateLocation.getYaw(), templateLocation.getPitch());
            arenaSetup.addTeam(spawn);
            for (TemplateLocation chestTemplateLocation : spawnChests.get(i)) {
                Location chestSpawn = new Location(arenaSetup.getWorld(),
                        chestTemplateLocation.getX(), chestTemplateLocation.getY(), chestTemplateLocation.getZ());
                arenaSetup.addChest(chestSpawn.getBlock(), ArenaSetupStage.SPAWN);
            }
        }
        //Add mid-chests
        for (TemplateLocation midTemplateLocation : midChests) {
            Location chestSpawn = new Location(arenaSetup.getWorld(),
                    midTemplateLocation.getX(), midTemplateLocation.getY(), midTemplateLocation.getZ());
            arenaSetup.addChest(chestSpawn.getBlock(), ArenaSetupStage.MID);
        }
    }
}
