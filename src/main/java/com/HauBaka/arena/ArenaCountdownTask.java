package com.HauBaka.arena;

import com.HauBaka.Skywars;
import com.HauBaka.enums.ArenaState;
import com.HauBaka.enums.PlaceholderVariable;
import com.HauBaka.object.ArenaChest;
import com.HauBaka.player.GamePlayer;
import com.HauBaka.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ArenaCountdownTask {
    private int taskID;
    private BukkitTask chestHologramTask;
    private final Arena arena;
    ArenaCountdownTask(Arena arena) {
        this.arena = arena;
    }
    public void cancelTask() { Bukkit.getScheduler().cancelTask(taskID); }
    private void countdown() { arena.setTime(arena.getTime() - 1);}
    public void starting() {
        String message = Skywars.getMessageConfig().getConfig().getString("arena.start-countdown");
        String msgSeconds = Skywars.getMessageConfig().getConfig().getString("arena.countdown-seconds");
        String msgSecond = Skywars.getMessageConfig().getConfig().getString("arena.countdown-second");

        arena.broadcast(message.replace ("%time%", "§a" + arena.getTime()));

        doTimerLoop(() -> {
            int time = arena.getTime();
            if (time == 10)  arena.broadcast(msgSeconds.replace("%time%", "§6" + time));
            else if (time <= 5 && time > 1) arena.broadcast(msgSeconds.replace("%time%", "§c" + time));
            else if (time == 1) arena.broadcast(msgSecond.replace("%time%", "§c" + time));
            else if (time <= 0) {
                arena.setState(
                        Skywars.getConfigConfig().getConfig().getBoolean("waiting_lobby") ?
                                ArenaState.CAGE_OPENING : ArenaState.PHASE_1
                );
            }
        });
    }
    public void cage_opening() {
        for (ArenaTeam team : arena.getTeams()) {
            team.joinCage();
        }
        updateScoreboard();
        String msg_seconds = Skywars.getMessageConfig().getConfig().getString("arena.cage_opening-seconds");
        String msg_second = Skywars.getMessageConfig().getConfig().getString("arena.cage_opening-second");
        doTimerLoop(() -> {
            int time = arena.getTime();
            if (time == 10)  arena.broadcast(msg_seconds.replace("%time%", "§6" + time));
            else if (time <= 5 && time > 1)  arena.broadcast(msg_seconds.replace("%time%", "§c" + time));
            else if (time == 1) arena.broadcast(msg_second.replace("%time%", "§c" + time));
        });
    }
    public void doTimerLoop(Runnable runnable) {
        updateChestsPacket();
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Skywars.getInstance(),
                () -> {
                    updateTimerScoreboard();
                    updateChests();
                    runnable.run();
                    if (arena.getTime() == 0) {
                        arena.setState(arena.getState().getNext());
                    }
                    countdown();
                }, 0L, 20L);
    }
    public void updateChests() {
        if (arena.getState() == ArenaState.AVAILABLE
                || arena.getState() == ArenaState.WAITING
                || arena.getState() == ArenaState.STARTING
                || arena.getState() == ArenaState.CAGE_OPENING) return;
        String time = "&a" + Utils.secondsToTime(arena.getTime());
        for (ArenaChest chest : arena.getOpenedChests()) {
            chest.getHologram().setLine(0, time);
        }
    }
    public void updateChestsPacket() {
        if (chestHologramTask != null) chestHologramTask.cancel();
        chestHologramTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (ArenaChest chest : arena.getOpenedChests()) {
                    chest.sendOpenPacket(true);
                }
            }
        }.runTaskTimer(Skywars.getInstance(), 0L, 3L);
    }
    private void updateTimerScoreboard() {
        if (arena.getState() == ArenaState.AVAILABLE ||
                arena.getState() == ArenaState.WAITING ||
                arena.getState() == ArenaState.STARTING) {

            Object o = ArenaState.WAITING;
            List<String> lines = ScoreboardData.getScoreboard(o);
            String value = arena.getState() == ArenaState.WAITING ? "Waiting" : "Starting in &a" + arena.getTime() + "s";
            PlaceholderVariable var = PlaceholderVariable.STATE;
            int idx = ScoreboardData.getIndex(o, var);
            String line = lines.get(idx).replace(var.getPlaceholder(), value);
            int displayIndex = lines.size() - idx - 1;

            for (GamePlayer gp : arena.getPlayers()) {
                gp.getScoreboard().setLine(displayIndex, line);
            }
            return;
        }

        Object mode = arena.getVariant().getMode();
        List<String> lines = new ArrayList<>(ScoreboardData.getScoreboard(mode));
        int idxTimer = ScoreboardData.getIndex(mode, PlaceholderVariable.TIMER);
        int idxNext = ScoreboardData.getIndex(mode, PlaceholderVariable.NEXT_EVENT_NAME);

        String timerLine = lines.get(idxTimer).replace(PlaceholderVariable.TIMER.getPlaceholder(), Utils.secondsToTime(arena.getTime()));
        lines.set(idxTimer, timerLine);

        String nextLine = lines.get(idxNext).replace(PlaceholderVariable.NEXT_EVENT_NAME.getPlaceholder(),
                arena.getState() == ArenaState.CAGE_OPENING ? arena.getState().getName() : arena.getState().getNext().getName());
        lines.set(idxNext, nextLine);

        int displayTimerIdx = lines.size() - idxTimer - 1;
        int displayNextIdx = lines.size() - idxNext - 1;

        for (GamePlayer gp : arena.getAlive_players()) {
            gp.getScoreboard().setLine(displayTimerIdx, lines.get(idxTimer));
            gp.getScoreboard().setLine(displayNextIdx, lines.get(idxNext));
        }
        for (GamePlayer gp : arena.getSpectators()) {
            gp.getScoreboard().setLine(displayTimerIdx, lines.get(idxTimer));
            gp.getScoreboard().setLine(displayNextIdx, lines.get(idxNext));
        }
    }

    public void updateScoreboard(GamePlayer gamePlayer, PlaceholderVariable variable, Object value) {
        Object o = (arena.getState() == ArenaState.AVAILABLE ||
                arena.getState() == ArenaState.WAITING ||
                arena.getState() == ArenaState.STARTING) ?
                ArenaState.WAITING : arena.getVariant().getMode();

        int idx = ScoreboardData.getIndex(o, variable);
        List<String> lines = ScoreboardData.getScoreboard(o);
        String line = lines.get(idx).replace(variable.getPlaceholder(), String.valueOf(value));
        gamePlayer.getScoreboard().setLine(lines.size() - idx - 1, line);
    }

    public void updateScoreboard() {
        for (GamePlayer p : arena.getPlayers()) updateScoreboard(p);
        for (GamePlayer s : arena.getSpectators()) updateScoreboard(s);
    }

    public void updateScoreboard(GamePlayer gamePlayer) {
        List<String> contents;
        Map<PlaceholderVariable, Object> data = new LinkedHashMap<>();
        data.put(PlaceholderVariable.DATE, Utils.getTodayFormat("dd/MM/yy"));
        data.put(PlaceholderVariable.GAME_ID, arena.getId());
        data.put(PlaceholderVariable.MAP_COLORED_NAME, arena.getName());
        data.put(PlaceholderVariable.MODE_COLORED_NAME, arena.getVariant().getType().toString());

        boolean preGame = arena.getState() == ArenaState.AVAILABLE
                || arena.getState() == ArenaState.WAITING
                || arena.getState() == ArenaState.STARTING;

        if (preGame) {
            contents = new ArrayList<>(ScoreboardData.getScoreboard(ArenaState.WAITING));
            data.put(PlaceholderVariable.GAME_PLAYERS, arena.getPlayers().size());
            data.put(PlaceholderVariable.GAME_MAX_PLAYERS, arena.getVariant().getMode().getMaxPlayer());
            int time = arena.getTime();
            data.put(PlaceholderVariable.STATE,
                    (arena.getState() == ArenaState.AVAILABLE || arena.getState() == ArenaState.WAITING) ?
                            "Waiting..." : "Starting in &a" + time + "s"
            );

            applyDataToContents(contents, ArenaState.WAITING, data);
            gamePlayer.getScoreboard().setContents(contents);
            return;
        }

        Object mode = arena.getVariant().getMode();
        contents = new ArrayList<>(ScoreboardData.getScoreboard(mode));
        data.put(PlaceholderVariable.NEXT_EVENT_NAME, arena.getState().getNext().getName());
        data.put(PlaceholderVariable.TIMER, Utils.secondsToTime(arena.getTime()));
        data.put(PlaceholderVariable.PLAYERS_LEFT, arena.getAlive_players().size());
        data.put(PlaceholderVariable.TEAMS_LEFT, arena.getAlive_teams().size());

        applyDataToContents(contents, mode, data);

        int killsLine = -1, assistsLine = -1;
        for (int i = 0; i < contents.size(); ++i) {
            String line = contents.get(i);
            if (line.contains(PlaceholderVariable.KILLS.getPlaceholder())) killsLine = i;
            if (line.contains(PlaceholderVariable.ASSISTS.getPlaceholder())) assistsLine = i;
        }

        List<String> cpy = new ArrayList<>(contents);
        if (arena.getPlayers().contains(gamePlayer)) {
            if (killsLine >= 0) {
                String line = cpy.get(killsLine).replace(PlaceholderVariable.KILLS.getPlaceholder(),
                        String.valueOf(arena.getKills().getOrDefault(gamePlayer, 0)));
                cpy.set(killsLine, line);
            }
            if (assistsLine >= 0) {
                String line = cpy.get(assistsLine).replace(PlaceholderVariable.ASSISTS.getPlaceholder(),
                        String.valueOf(arena.getAssists().getOrDefault(gamePlayer, 0)));
                cpy.set(assistsLine, line);
            }
        } else {
            if (killsLine >= 0) cpy.set(killsLine,  "Spectating...");
            if (assistsLine >= 0) cpy.remove(assistsLine);
        }
        gamePlayer.getScoreboard().setContents(cpy);
    }

    private void applyDataToContents(List<String> contents, Object scoreboardKey, Map<PlaceholderVariable, Object> data) {
        for (Map.Entry<PlaceholderVariable, Object> e : data.entrySet()) {
            PlaceholderVariable var = e.getKey();
            Object val = e.getValue();
            int idx = ScoreboardData.getIndex(scoreboardKey, var);
            if (idx >= 0 && idx < contents.size()) {
                contents.set(idx, contents.get(idx).replace(var.getPlaceholder(), String.valueOf(val)));
            }
        }
    }
}
