package com.HauBaka.arena;

import com.HauBaka.enums.ArenaState;
import com.HauBaka.enums.ArenaVariant;
import com.HauBaka.enums.PlaceholderVariable;
import com.HauBaka.file.FileConfig;
import lombok.Getter;
import org.bukkit.ChatColor;

import java.util.*;

public class ScoreboardData {
    private static Map<Object, List<String>> scoreboard_lines;
    private static Map<Object, Map<PlaceholderVariable, Integer>> variables;
    @Getter
    private static FileConfig config;
    @Getter
    private static String title;
    public static void init() {
        scoreboard_lines = new HashMap<>();
        variables = new HashMap<>();
        config = new FileConfig("scoreboard.yml");
        config.saveDefaultConfig();

        title = ChatColor.translateAlternateColorCodes(
                '&',
                config.getConfig().getString("title", "empty")
        );
        //load scoreboards
        load("waiting", ArenaState.WAITING);
        load("ingame_solo", ArenaVariant.Mode.SOLO);
        load("ingame_double", ArenaVariant.Mode.DOUBLES);
        load("ingame_mega", ArenaVariant.Mode.MEGA);
    }
    private static void load(String path, Object object) {
        List<String> lines = config.getConfig().getStringList(path);
        Map<PlaceholderVariable, Integer> variable = new LinkedHashMap<>();

        for (int i = 0; i < lines.size(); ++i) {
            String line = lines.get(i);
            for (PlaceholderVariable var : PlaceholderVariable.values()) {
                if (line.contains(var.getPlaceholder()))
                    variable.put(var, i);
            }
        }
        scoreboard_lines.put(object,lines);
        variables.put(object, variable);
    }
    public static List<String> getScoreboard(Object object) {
        return scoreboard_lines.get(object);
    }
    public static int getIndex(Object object, PlaceholderVariable placeholderVariable) {
        return variables.containsKey(object) ?
                variables.get(object).getOrDefault(placeholderVariable,  0) : 0;
    }
}
