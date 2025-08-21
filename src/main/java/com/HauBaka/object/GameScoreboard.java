package com.HauBaka.object;

import com.HauBaka.arena.Arena;
import com.HauBaka.arena.ArenaTeam;
import com.HauBaka.arena.ScoreboardData;
import com.HauBaka.enums.ArenaState;
import com.HauBaka.enums.ScoreboardVariable;
import com.HauBaka.player.GamePlayer;
import com.HauBaka.utils.Utils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;

public class GameScoreboard {

    private static final int MAX_LINES = 15;
    private int size;
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
        this.size = 0;
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
        Collections.reverse(contents);
        List<String> list = new ArrayList<>(contents);
        if (list.size() > MAX_LINES) list = list.subList(0, MAX_LINES);

        this.size = Math.min(MAX_LINES, contents.size());
        for (int i = 0; i < size; i++) {
            setLine(i, list.get(i));
        }
        for (int i = size; i < MAX_LINES; i++) {
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
            team = scoreboard.getTeam(teamName);
            if (team == null) team = scoreboard.registerNewTeam(teamName);
            lineTeams.put(index, team);
        }
        if (!team.hasEntry(entry)) {
            objective.getScore(entry).setScore(index+1);
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

    public void setArena(Arena arena, ArenaTeam myTeam) {
        World arenaWorld = arena.getWorld();

        Objective healthUnder = scoreboard.getObjective("health_under");
        if (healthUnder == null) {
            healthUnder = scoreboard.registerNewObjective("health_under", "health");
            healthUnder.setDisplaySlot(DisplaySlot.BELOW_NAME);
            healthUnder.setDisplayName("§c❤");
        }

        Objective healthTab = scoreboard.getObjective("health_tablist");
        if (healthTab == null) {
            healthTab = scoreboard.registerNewObjective("health_tablist", "dummy");
            healthTab.setDisplaySlot(DisplaySlot.PLAYER_LIST);
            healthTab.setDisplayName("HP");
        }

        for (Team oldTeam : new ArrayList<>(scoreboard.getTeams())) {
            try { oldTeam.unregister(); } catch (Exception ignored) {}
        }

        for (ArenaTeam team : arena.getTeams()) {
            String teamName = team.equals(myTeam) ? "000_" + team.getName() : String.valueOf(team.getName());
            Team sbTeam = scoreboard.getTeam(teamName);
            if (sbTeam == null) sbTeam = scoreboard.registerNewTeam(teamName);

            sbTeam.setPrefix((team.equals(myTeam) ? "§a" : "§c") + "[" + team.getName() + "] ");
            for (GamePlayer gp : team.getMembers()) {
                Player p = gp.getPlayer();
                if (p == null || !p.isOnline()) continue;
                if (!arena.getAlive_players().contains(gp)) continue;
                if (!p.getWorld().equals(arenaWorld)) continue;

                sbTeam.addEntry(p.getName());
                Score score = healthTab.getScore(p.getName());
                score.setScore((int) p.getHealth());
            }
        }

        Team spectator = scoreboard.getTeam("zzz_spectator");
        if (spectator == null) spectator = scoreboard.registerNewTeam("zzz_spectator");
        spectator.setPrefix("§7[SPEC] ");

        for (GamePlayer spec : arena.getSpectators()) {
            Player p = spec.getPlayer();
            if (p == null || !p.isOnline()) continue;
            if (!p.getWorld().equals(arenaWorld)) continue;

            spectator.addEntry(p.getName());
        }
    }
    public void removeArena() {
        Objective healthUnder = scoreboard.getObjective("health_under");
        if (healthUnder != null) {
            try { healthUnder.unregister(); } catch (Exception ignored) {}
        }

        Objective healthTab = scoreboard.getObjective("health_tablist");
        if (healthTab != null) {
            try { healthTab.unregister(); } catch (Exception ignored) {}
        }

        for (Team t : new ArrayList<>(scoreboard.getTeams())) {
            try { t.unregister(); } catch (Exception ignored) {}
        }

    }
    public void setSpectator() {
        Arena arena = gamePlayer.getArena();
        if (arena == null) return;

        Team spectator = scoreboard.getTeam("zzz_spectator");
        if (spectator == null) {
            spectator = scoreboard.registerNewTeam("zzz_spectator");
            spectator.setPrefix("§7[SPEC] ");
        }
        spectator.addEntry(gamePlayer.getPlayer().getName());

        Objective healthUnder = scoreboard.getObjective("health_under");
        if (healthUnder != null) healthUnder.getScoreboard().resetScores(gamePlayer.getPlayer().getName());

        Objective healthTab = scoreboard.getObjective("health_tablist");
        if (healthTab != null) healthTab.getScoreboard().resetScores(gamePlayer.getPlayer().getName());
    }

    public void addToTablist(GamePlayer gp, ArenaTeam team, boolean isMyTeam) {
        String teamName = isMyTeam ? "000_" + team.getName() : String.valueOf(team.getName());
        Team sbTeam = scoreboard.getTeam(teamName);
        if (sbTeam == null) {
            sbTeam = scoreboard.registerNewTeam(teamName);
            sbTeam.setPrefix((isMyTeam ? "§a" : "§c") + "[" + team.getName() + "] ");
        }
        sbTeam.addEntry(gp.getPlayer().getName());

        Objective healthTab = scoreboard.getObjective("health_tablist");
        if (healthTab != null) {
            Score score = healthTab.getScore(gp.getPlayer().getName());
            score.setScore((int) gp.getPlayer().getHealth());
        }
    }
    public void removeFromTablist(GamePlayer gp) {
        for (Team t : scoreboard.getTeams()) {
            if (t.hasEntry(gp.getPlayer().getName())) {
                t.removeEntry(gp.getPlayer().getName());
            }
        }
        Objective healthTab = scoreboard.getObjective("health_tablist");
        if (healthTab != null) {
            healthTab.getScoreboard().resetScores(gp.getPlayer().getName());
        }
    }

}
