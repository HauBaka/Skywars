package com.HauBaka.object.cage;

import com.HauBaka.Skywars;
import com.HauBaka.enums.ObjectRarity;
import com.HauBaka.file.FileConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CageManager {
    private static Map<String, Cage> cageList;
    public static void init() {
        cageList = new HashMap<>();

        File folder = new File(Skywars.getInstance().getDataFolder(), "cages");
        if (!folder.exists()) {
            folder.mkdirs();
            FileConfig defaultCage = new FileConfig("cages/default.yml");
            defaultCage.saveDefaultConfig();
            return;
        }

        File[] files = folder.listFiles((f, name) -> name.endsWith(".yml"));
        if (files == null) return;
        for (File file : files) {
            FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);
            load(fileConfiguration);
        }
        Bukkit.getLogger().info("Loaded " + cageList.size() + " cages");
    }

    private static void load(FileConfiguration config) {
        String name = config.getString("name");
        ObjectRarity rarity = ObjectRarity.valueOf(config.getString("rarity", "COMMON"));
        List<Map<?, ?>> blockList = config.getMapList("blocks");

        List<Cage.CageBlock> blocks = parseBlocks(blockList);

        Cage cage = new Cage(name, blocks, rarity);
        cageList.put(name.toLowerCase(), cage);

    }

    private static List<Cage.CageBlock> parseBlocks(List<Map<?, ?>> blockList) {
        List<Cage.CageBlock> blocks = new ArrayList<>();
        for (Map<?, ?> entry : blockList) {
            String type =  entry.containsKey("type") ? (String) entry.get("type") : "SINGLE";
            String matName = (String) entry.get("material");

            Material material = Material.valueOf(matName.toUpperCase());
            byte data = entry.containsKey("data") ? ((Number) entry.get("data")).byteValue() : 0;

            switch (type.toUpperCase()) {
                case "CUBE":
                    List<Integer> from = (List<Integer>) entry.get("from");
                    List<Integer> to = (List<Integer>) entry.get("to");

                    for (int x = from.get(0); x <= to.get(0); x++) {
                        for (int y = from.get(1); y <= to.get(1); y++) {
                            for (int z = from.get(2); z <= to.get(2); z++) {
                                if ((x == from.get(0) || x == to.get(0)) ||
                                        (y == from.get(1) || y == to.get(1)) ||
                                        (z == from.get(2) || z == to.get(2))) {
                                    blocks.add(new Cage.CageBlock(x, y, z, material, data));
                                }
                            }
                        }
                    }
                    break;
                case "SINGLE":
                    int x = ((Number) entry.get("x")).intValue();
                    int y = ((Number) entry.get("y")).intValue();
                    int z = ((Number) entry.get("z")).intValue();
                    blocks.add(new Cage.CageBlock(x, y, z, material, data));
                    break;
            }
        }
        return blocks;
    }
    public static Cage getCage(String name) {
        name = name.toLowerCase();
        return cageList.getOrDefault(name, null);
    }
    public static List<Cage> getByRarity(ObjectRarity rarity) {
        List<Cage> cages = new ArrayList<>();
        for (Cage cage : cageList.values()) {
            if (cage.getRarity()==rarity) cages.add(cage);
        }
        return cages;
    }
    public static Cage createCage(String name, List<Cage.CageBlock> blocks, ObjectRarity rarity) {
        Cage cage = new Cage(name, blocks, rarity);
        cageList.put(name.toLowerCase(), cage);
        return cage;
    }
}
