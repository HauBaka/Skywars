package com.HauBaka.npc;

import com.HauBaka.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class NPCManager implements Listener {
    private static final String[] letters = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "X", "W", "Y", "Z", "0", "1",  "2", "3", "4", "5", "6", "7", "8", "9", "0"};

    private static List<NPC> NPCs;

    public static void init() {
        NPCs = new ArrayList<>();
    }
    public static NPC createNPC(Location location, NPCType type, String name) {
        NPC npc = new NPC(location, type, name);
        NPCs.add(npc);
        return npc;
    }
    public static NPC createNPC(Location location, NPCType type) {
        return createNPC(location, type, null);
    }

    public static void removeNPC(NPC npc) {
        if (!NPCs.contains(npc)) {
            return;
        }
        NPCs.remove(npc);
        npc.destroy();
    }
    public static NPC getNPCFromName(String name) {
        for (NPC npc : NPCs) {
            if (npc.getName().equalsIgnoreCase(name)) {
                return npc;
            }
        }
        return null;
    }
    public NPC getNPCFromEntity(Entity entity) {
        for (NPC npc : NPCs) {
            if (npc.getNpc() == entity) {
                return npc;
            }
        }
        return null;
    }
    public static String generateNPCName() {
        StringBuilder name = new StringBuilder();
        while (name.length() == 0 || getNPCFromName(name.toString()) != null) {
            for (int i = 0; i < 5; i++) {
                name.append(letters[Utils.randomInRange(0, letters.length - 1)]);
            }
        }

        return "n" + name.toString();
    }

}

