package com.HauBaka.arena;

import com.HauBaka.Skywars;
import com.HauBaka.enums.ArenaState;
import com.HauBaka.enums.ArenaVariant;
import com.HauBaka.enums.ScoreboardVariable;
import com.HauBaka.player.GamePlayer;
import com.HauBaka.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ArenaCountdownTask {
    private int taskID;
    private final Arena arena;
    ArenaCountdownTask(Arena arena) {
        this.arena = arena;
    }
    public void cancelTask() {
        Bukkit.getScheduler().cancelTask(taskID);
    }
    public void waiting() {
    }
    private void countdown() {
        arena.setTime(arena.getTime() - 1);
    }
    //TODO: Update scoreboard
    public void starting() {
        String message = ChatColor.translateAlternateColorCodes(
                '&',
                Skywars.getInstance().getMessageConfig().getConfig().getString("arena.start-countdown"));
        String msgSeconds = ChatColor.translateAlternateColorCodes(
                '&',
                Skywars.getInstance().getMessageConfig().getConfig().getString("arena.countdown-seconds"));
        String msgSecond = ChatColor.translateAlternateColorCodes(
                '&',
                Skywars.getInstance().getMessageConfig().getConfig().getString("arena.countdown-second"));

        arena.broadcast(message.replace ("%time%", "§a" + arena.getTime()));
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Skywars.getInstance(),
                new Runnable() {
                    @Override
                    public void run() {
                        updateTimerScoreboard();
                        int time = arena.getTime();
                        if (time == 10) {
                            arena.broadcast(msgSeconds.replace("%time%", "§6" + time));
                        } else if (time <= 5 && time > 1) {
                            arena.broadcast(msgSeconds.replace("%time%", "§c" + time));
                        } else if (time == 1) {
                            arena.broadcast(msgSecond.replace("%time%", "§c" + time));
                        } else if (time <= 0) {
                            arena.removeLobby();
                            arena.setState(
                                    Skywars.getConfigConfig().getConfig().getBoolean("waiting_lobby") ?
                                            ArenaState.CAGE_OPENING : ArenaState.PHASE_1
                            );
                        }
                        countdown();
                    }
                }, 0L, 20L);
    }
    public void cage_opening() {
        for (ArenaTeam team : arena.getTeams()) {
            team.joinCage();
        }
        updateScoreboard();
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Skywars.getInstance(),
                new Runnable() {
                    String message = Skywars.getInstance().getMessageConfig().getConfig().getString("arena.cage_opening-seconds");
                    @Override
                    public void run() {
                        updateTimerScoreboard();
                        int time = arena.getTime();
                        if (time == 10)  arena.broadcast(
                                message.replace("%time%", "§6" + time)
                                        .replace("&", "§")
                        );
                        else if (time < 5 && time > 1)  arena.broadcast(
                                message.replace("%time%", "§c" + time)
                                        .replace("&", "§")
                        );
                        else if (time == 1) {
                            message = Skywars.getInstance().getMessageConfig().getConfig().getString("arena.cage_opening-second");
                            arena.broadcast(
                                    message.replace("%time%", "§c" + time)
                                            .replace("&", "§")
                            );
                        }
                        else if (time <= 0) {
                            arena.setState(ArenaState.PHASE_1);
                        }
                        countdown();
                    }
                }, 0L, 20L);
    }

    public void phase_1() {
        arena.refill();
        for (ArenaTeam team : arena.getTeams())
            team.removeCage();
        ingame_timer();

    }
    public void phase_2() {
        arena.refill();
        ingame_timer();
    }
    public void phase_3() {
        arena.refill();
        ingame_timer();
    }

    public void doom() {
        arena.refill();
        ingame_timer();
    }

    public void ending() {

    }

    private void ingame_timer() {
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Skywars.getInstance(),
                new Runnable() {
                    @Override
                    public void run() {
                        int time = arena.getTime();
                        updateTimerScoreboard();
                        if (time == 0)
                            arena.setState(arena.getState().getNext());
                        countdown();
                    }
                }, 0L, 20L);
    }
    private void updateTimerScoreboard() {
        Object o;
        List<String> lines;
        ScoreboardVariable var;
        if (arena.getState() == ArenaState.AVAILABLE ||
                arena.getState() == ArenaState.WAITING ||
                arena.getState() == ArenaState.STARTING) {
            o = ArenaState.WAITING;
            lines = ScoreboardData.getScoreboard(o);

            String value = arena.getState() == ArenaState.WAITING ? "Waiting" : "Starting in &a" + arena.getTime();

            var = ScoreboardVariable.STATE;
            int idx = ScoreboardData.getIndex(o, var);
            String line = lines.get(idx).replace(var.getPlaceholder(), value);
            idx = lines.size() - idx - 1;
            for (GamePlayer gamePlayer : arena.getPlayers())
                gamePlayer.getScoreboard().setLine(idx, line);
            return;
        }
        o =  arena.getVariant().getMode();

        lines = ScoreboardData.getScoreboard(o);
        int idx_timer = ScoreboardData.getIndex(o, ScoreboardVariable.TIMER);
        int idx_next_event = ScoreboardData.getIndex(o, ScoreboardVariable.NEXT_EVENT_NAME);

        lines.set(idx_timer, lines.get(idx_timer).replace(ScoreboardVariable.TIMER.getPlaceholder(), Utils.secondsToTime(arena.getTime())));
        lines.set(idx_next_event, lines.get(idx_next_event).replace(
                ScoreboardVariable.NEXT_EVENT_NAME.getPlaceholder(),
                arena.getState() == ArenaState.CAGE_OPENING ? arena.getState().getName() : arena.getState().getNext().getName()));

        for (GamePlayer gamePlayer : arena.getAlive_players()) {
            gamePlayer.getScoreboard().setLine(lines.size() - idx_timer - 1, lines.get(idx_timer));
            gamePlayer.getScoreboard().setLine(lines.size() - idx_next_event - 1, lines.get(idx_next_event));
        }

        for (GamePlayer gamePlayer : arena.getSpectators()) {
            gamePlayer.getScoreboard().setLine(lines.size() - idx_timer - 1, lines.get(idx_timer));
            gamePlayer.getScoreboard().setLine(lines.size() - idx_next_event - 1, lines.get(idx_next_event));
        }
    }
    public void updateScoreboard(GamePlayer gamePlayer, ScoreboardVariable variable, Object value) {
        Object o = (arena.getState() == ArenaState.AVAILABLE ||
                    arena.getState() == ArenaState.WAITING ||
                    arena.getState() == ArenaState.STARTING) ?
                ArenaState.WAITING : arena.getVariant().getMode();
        int idx = ScoreboardData.getIndex(o, variable);
        String line = ScoreboardData.getScoreboard(o).get(idx).replace(variable.getPlaceholder(), String.valueOf(value));

        gamePlayer.getScoreboard().setLine(idx, line);
    }
    public void updateScoreboard() {
        List<String> contents;

        Map<ScoreboardVariable, Object> data = new LinkedHashMap<>();
        data.put(ScoreboardVariable.DATE, Utils.getTodayFormat("dd/MM/yy"));
        data.put(ScoreboardVariable.GAME_ID, arena.getId());
        data.put(ScoreboardVariable.MAP_COLORED_NAME, arena.getName());
        data.put(ScoreboardVariable.MODE_COLORED_NAME, arena.getVariant().getType().toString());

        if (arena.getState() == ArenaState.AVAILABLE || arena.getState() == ArenaState.WAITING || arena.getState() == ArenaState.STARTING) {
            contents = ScoreboardData.getScoreboard(ArenaState.WAITING);

            data.put(ScoreboardVariable.GAME_PLAYERS, arena.getPlayers().size());
            data.put(ScoreboardVariable.GAME_MAX_PLAYERS, arena.getVariant().getMode().getMaxPlayer());
            int time = arena.getTime();
            data.put(ScoreboardVariable.STATE, (arena.getState() == ArenaState.AVAILABLE || arena.getState() == ArenaState.WAITING) ?
                    "Waiting..." : "Starting in &a" + time +"s"
            );
            for (ScoreboardVariable var : data.keySet()) {
                int idx = ScoreboardData.getIndex(ArenaState.WAITING,var);
                contents.set(idx, contents.get(idx).replace(var.getPlaceholder(),String.valueOf(data.get(var))));
            }
            for (GamePlayer gamePlayer : arena.getPlayers()) {
                gamePlayer.getScoreboard().setContents(contents);
            }
            return;
        }
        contents = ScoreboardData.getScoreboard(arena.getVariant().getMode());

        data.put(ScoreboardVariable.NEXT_EVENT_NAME, arena.getState().getNext().getName());
        data.put(ScoreboardVariable.TIMER, Utils.secondsToTime(arena.getTime()));
        data.put(ScoreboardVariable.PLAYERS_LEFT, arena.getAlive_players().size());
        data.put(ScoreboardVariable.TEAMS_LEFT, arena.getAlive_teams().size());

        int killsLine = -1, assistsLine = -1;
        for (int i = 0; i < contents.size(); ++i) {
            String line = contents.get(i);
            if (line.contains(ScoreboardVariable.KILLS.getPlaceholder()))
                killsLine = i;
            if (line.contains(ScoreboardVariable.ASSISTS.getPlaceholder()))
                assistsLine = i;
        }

        for (ScoreboardVariable var : data.keySet()) {
            int idx = ScoreboardData.getIndex(arena.getVariant().getMode(),var);
            contents.set(idx, contents.get(idx).replace(var.getPlaceholder(),String.valueOf(data.get(var))));
        }
        // Update for alive players
        for (GamePlayer alivePlayer : arena.getAlive_players()) {
            List<String> cpyContents = new ArrayList<>(contents);
            if (killsLine >= 0) {
                String line = cpyContents.get(killsLine);
                line = line.replace(ScoreboardVariable.KILLS.getPlaceholder(),
                        String.valueOf(arena.getKills().getOrDefault(alivePlayer, 0)));
                cpyContents.set(killsLine, line);
            }
            if (assistsLine >= 0) {
                String line = cpyContents.get(assistsLine);
                line = line.replace(ScoreboardVariable.ASSISTS.getPlaceholder(),
                        String.valueOf(arena.getAssists().getOrDefault(alivePlayer, 0)));
                cpyContents.set(assistsLine, line);
            }
            alivePlayer.getScoreboard().setContents(cpyContents);
        }

        // Update for spectators
        for (GamePlayer specPlayer : arena.getSpectators()) {
            List<String> cpyContents = new ArrayList<>(contents);
            if (killsLine >= 0) {
                String line = cpyContents.get(killsLine);
                line = line.replace(ScoreboardVariable.KILLS.getPlaceholder(), "Spectating...");
                cpyContents.set(killsLine, line);
            }
            if (assistsLine >= 0) {
                cpyContents.remove(assistsLine);
            }
            specPlayer.getScoreboard().setContents(cpyContents);
        }
    }
}
