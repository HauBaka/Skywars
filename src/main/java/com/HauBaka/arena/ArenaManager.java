package com.HauBaka.arena;


import com.HauBaka.utils.Utils;
import com.HauBaka.enums.ArenaVariant;
import com.HauBaka.object.ArenaChest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ArenaManager {
    private static HashMap<String, Arena> arenaList;

    public static void init() {
        arenaList = new HashMap<>();
        ArenaChest.init();
    }

    public static Arena createArena(TemplateArena templateArena, ArenaVariant variant) {
        Arena arena = new Arena(templateArena, variant);
        arenaList.put(arena.getId(), arena);
        return arena;
    }
    public static String generateID() {
        String id = "";
        while (id.isEmpty() || arenaList.containsKey(id)) {
            id = "mini" + Utils.generateID(4);
        }
        return id;
    }

    public static List<Arena> getByVariant(ArenaVariant variant) {
        List<Arena> list = new ArrayList<>();
        for (Arena arena : arenaList.values()) {
            if (arena.getVariant() == variant) {
                list.add(arena);
            }
        }
        return list;
    }

    public static List<Arena> getByMap(String mapName) {
        List<Arena> list = new ArrayList<>();
        for (Arena arena : arenaList.values()) {
            if (arena.getTemplateArena().getMapName().equalsIgnoreCase(mapName)) {
                list.add(arena);
            }
        }
        return list;
    }

}
