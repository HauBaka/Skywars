package com.HauBaka.object;

import com.HauBaka.player.GamePlayer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;

public class GameScoreboard {

    private static final int MAX_LINES = 15;

    @Getter
    private final Scoreboard scoreboard;
    private Objective objective;

    private final Map<Integer, Team> lineTeams = new HashMap<>();
    private final String[] entries = buildEntries();

    private String title = ChatColor.RESET.toString();

    private final GamePlayer gamePlayer;

    public GameScoreboard(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        String objName = ("sw-" + gamePlayer.getPlayer().getUniqueId().toString().replace("-", ""))
                .substring(0, Math.min(16, ("sw-" + gamePlayer.getPlayer().getUniqueId()).length()));

        this.objective = scoreboard.registerNewObjective(objName, "dummy");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.objective.setDisplayName(this.title);
    }

    public void setTitle(String title) {
        this.title = colorize(title);
        try {
            objective.setDisplayName(this.title);
        } catch (IllegalStateException ignored) {
        }
    }

    public void setContents(List<String> contents) {
        if (contents == null) contents = Collections.emptyList();

        List<String> list = new ArrayList<>(contents);
        if (list.size() > MAX_LINES) list = list.subList(0, MAX_LINES);

        int n = list.size();
        for (int i = 0; i < n; i++) {
            setLine(i, list.get(i));
        }

        for (int i = n; i < MAX_LINES; i++) {
            clearLine(i);
        }
    }

    public void setLine(int index, String raw) {
        if (index < 0 || index >= MAX_LINES) return;

        String text = colorize(raw);
        String entry = entries[index];

        Team team = lineTeams.get(index);
        if (team == null || !scoreboard.getTeams().contains(team)) {
            String teamName = ("line-" + index);
            if (teamName.length() > 16) teamName = teamName.substring(0, 16);

            team = scoreboard.getTeam(teamName);
            if (team == null) team = scoreboard.registerNewTeam(teamName);
            lineTeams.put(index, team);
        }

        if (!team.hasEntry(entry)) {
            objective.getScore(entry).setScore(MAX_LINES - index);
            team.addEntry(entry);
        }

        applyTextToTeam(team, text);
    }

    public void clearLine(int index) {
        if (index < 0 || index >= MAX_LINES) return;
        String entry = entries[index];

        scoreboard.resetScores(entry);

        Team team = lineTeams.remove(index);
        if (team != null) {
            if (team.hasEntry(entry)) team.removeEntry(entry);
            try { team.unregister(); } catch (IllegalStateException ignored) {}
        }
    }

    public void show() {
        Player p = gamePlayer.getPlayer();
        if (p != null && p.isOnline()) {
            p.setScoreboard(scoreboard);
        }
    }

    public void hide() {
        Player p = gamePlayer.getPlayer();
        if (p != null && p.isOnline()) {
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }

    public void destroy() {
        for (int i = 0; i < MAX_LINES; i++) clearLine(i);
        try { objective.unregister(); } catch (Exception ignored) {}
    }


    private static String colorize(String s) {
        return s == null ? "" : ChatColor.translateAlternateColorCodes('&', s);
    }

    /**
     * Chia text vào team prefix/suffix (mỗi phần tối đa 16 char ở 1.8, kể cả mã màu).
     * Giữ màu ở suffix bằng ChatColor.getLastColors(prefix).
     */
    private static void applyTextToTeam(Team team, String text) {
        if (text == null) text = "";

        if (text.length() <= 16) {
            safeSetPrefix(team, text);
            safeSetSuffix(team, "");
            return;
        }

        String prefix = text.substring(0, 16);
        if (prefix.endsWith("§")) {
            prefix = prefix.substring(0, 15);
        }

        String rest = text.substring(prefix.length());
        String colorCarry = ChatColor.getLastColors(prefix);
        String suffix = colorCarry + rest;

        if (suffix.length() > 16) {
            suffix = suffix.substring(0, 16);
            if (suffix.endsWith("§")) {
                suffix = suffix.substring(0, 15);
            }
        }

        safeSetPrefix(team, prefix);
        safeSetSuffix(team, suffix);
    }

    private static void safeSetPrefix(Team team, String s) {
        try { team.setPrefix(s); } catch (IllegalArgumentException ignored) {}
    }

    private static void safeSetSuffix(Team team, String s) {
        try { team.setSuffix(s); } catch (IllegalArgumentException ignored) {}
    }

    private static String[] buildEntries() {
        String[] arr = new String[MAX_LINES];
        List<ChatColor> pool = new ArrayList<>();
        for (ChatColor cc : ChatColor.values()) {
            if (cc == ChatColor.RESET) continue;
            try {
                if (cc.isColor() || cc == ChatColor.MAGIC) {
                    pool.add(cc);
                }
            } catch (NoSuchMethodError e) {
                pool.add(cc);
            }
        }
        for (int i = 0; i < MAX_LINES; i++) {
            if (i < pool.size()) {
                arr[i] = pool.get(i).toString();
            } else {
                arr[i] = ChatColor.WHITE.toString() + i;
            }
        }
        return arr;
    }
}
