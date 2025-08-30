package com.HauBaka.lobby;

import com.HauBaka.Skywars;
import com.HauBaka.arena.Arena;
import com.HauBaka.npc.NPC;
import com.HauBaka.npc.NPCManager;
import com.HauBaka.npc.NPCType;
import com.HauBaka.object.block.AdvancedBlock;
import com.HauBaka.player.GamePlayer;
import com.HauBaka.world.WorldManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;

@Getter
public class Lobby implements Listener {
    private static List<AdvancedBlock> soulWells;
    private static List<NPC> NPCs;
    private static Location location;
    private static Configuration config;
    public static void init() {
        config = Skywars.getConfigConfig().getConfig();

        soulWells = new ArrayList<>();
        NPCs = new ArrayList<>();
        if (config.contains("lobby")) {
            if (!config.contains("lobby.source")) {
                return;
            }
            WorldManager.cloneWorld(config.getString("lobby.source"), w -> {
                initSpawn(w);
                initNPCs();
                addPlayers();
            });
        } else {
            Bukkit.getLogger().warning("No lobby config found!");
        }
    }

    private static void addPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            joinLobby(GamePlayer.getGamePlayer(player));
        }
    }

    private static void initNPCs() {
        ConfigurationSection section = config.getConfigurationSection("lobby.npcs");
        if (section == null) {
            return;
        }

        for (String npcName : section.getKeys(false)) {
            String raw = section.getString(npcName);
            if (raw == null) continue;

            String[] split = raw.split(";");
            if (split.length < 6) continue;

            try {
                Location npcLocation = new Location(
                        location.getWorld(),
                        Double.parseDouble(split[0]),
                        Double.parseDouble(split[1]),
                        Double.parseDouble(split[2])
                );
                StringBuilder cmd = new StringBuilder();
                for (int i = 5; i < split.length; i++) cmd.append(split[i]).append(" ");

                NPC npc = NPCManager.createNPC(npcLocation, NPCType.valueOf(split[3].toUpperCase()));
                npc.setClickEvent(p -> p.performCommand(cmd.toString()));
                npc.setSkin(split[4]);
                NPCs.add(npc);
            } catch (Exception ignored) {
            }
        }
    }

    public static void addNPC(NPC npc) {
        NPCs.add(npc);

        Location loc = npc.getNpc().getBukkitEntity().getLocation();
        String npcString = loc.getX() + ";" + loc.getY() + ";" + loc.getZ()
                + ";" + npc.getType().toString()
                + ";" + npc.getSkinName()
                + ";" + npc.getCmd();

        ConfigurationSection section = config.getConfigurationSection("lobby.npcs");
        if (section == null) {
            section = config.createSection("lobby.npcs");
        }

        section.set(npc.getName(), npcString);
        Skywars.getConfigConfig().saveConfig();
    }

    public static boolean removeNPC(NPC npc) {
        NPCs.remove(npc);
        NPCManager.removeNPC(npc);

        ConfigurationSection section = config.getConfigurationSection("lobby.npcs");
        if (section == null) {
            return false;
        }

        String targetKey = null;
        for (String key : section.getKeys(false)) {
            String raw = section.getString(key);
            if (raw == null) continue;

            String[] split = raw.split(";");
            if (split.length != 6) continue;

            if (split[3].equalsIgnoreCase(npc.getType().name())) {
                targetKey = key;
                break;
            }
        }

        if (targetKey != null) {
            section.set(targetKey, null);
            return true;
        }
        return false;
    }

    private static void initSpawn(World w) {
        location = w.getSpawnLocation();
        location.setX(config.getDouble("lobby.x"));
        location.setY(config.getDouble("lobby.y"));
        location.setZ(config.getDouble("lobby.z"));
        location.setYaw(Float.parseFloat(config.getString("lobby.yaw")));
    }
    public static void joinLobby(GamePlayer gamePlayer) {
        if (gamePlayer == null) return;

        Arena arena = gamePlayer.getArena();
        if (arena != null) arena.removePlayer(gamePlayer);
        gamePlayer.setArena(null);

        gamePlayer.getPlayer().teleport(location);
        for (NPC npc : NPCs) npc.show(gamePlayer.getPlayer());


    }

    public static void setLocation(Location location) {
        config.set("lobby.x", location.getX());
        config.set("lobby.y", location.getY());
        config.set("lobby.z", location.getZ());
        config.set("lobby.yaw", location.getYaw());
        Skywars.getConfigConfig().saveConfig();

        Lobby.location = location;
    }

    public static void setSource(String s) {
        config.set("lobby.source", s);
        Skywars.getConfigConfig().saveConfig();
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        GamePlayer gamePlayer = GamePlayer.getGamePlayer(event.getPlayer());
        joinLobby(gamePlayer);
    }
}
