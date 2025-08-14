package com.HauBaka.arena.setup;

import com.HauBaka.player.GamePlayer;
import org.bukkit.ChatColor;

import java.util.Map;

public class ArenaSetupManager {
    private static Map<String, ArenaSetup> maps;
    public static ArenaSetup createEdit(String sourceWorldName, GamePlayer editor) {
        sourceWorldName=sourceWorldName.toLowerCase();
        if (maps.containsKey(sourceWorldName)) {
            editor.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4&lERROR!&c Someone was editing this map!"));
            return null;
        }
        ArenaSetup arenaSetup = new ArenaSetup(sourceWorldName,editor);
        maps.put(sourceWorldName, arenaSetup);
        return  arenaSetup;
    }
    public static ArenaSetup getByEditor(GamePlayer editor) {
        for (ArenaSetup arenaSetup : maps.values()) if (arenaSetup.getEditor() == editor) return arenaSetup;
        return null;
    }
}
