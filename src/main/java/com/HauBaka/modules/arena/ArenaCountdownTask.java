package com.HauBaka.modules.arena;

import com.HauBaka.Skywars;
import com.HauBaka.modules.arena.enums.ArenaState;
import org.bukkit.Bukkit;

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
    //TODO: Update scoreboard
    public void starting() {
        int time = arena.getState().getTime();
        String message = Skywars.getInstance().getMessageConfig().getConfig().getString("arena.start-countdown");
        arena.broadcast(
                message.replace ("%time%", "§a" + time)
                        .replace("&", "§")
        );

        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Skywars.getInstance(),
                new Runnable() {
                    int time = arena.getState().getTime();
                    String message = Skywars.getInstance().getMessageConfig().getConfig().getString("arena.countdown-seconds");
                    @Override
                    public void run() {
                        --time;
                        if (time == 10)  arena.broadcast(
                                message.replace("%time%", "§6" + time)
                                        .replace("&", "§")
                        );
                        else if (time < 5 && time > 1)  arena.broadcast(
                                message.replace("%time%", "§c" + time)
                                        .replace("&", "§")
                        );
                        else if (time == 1) {
                            message = Skywars.getInstance().getMessageConfig().getConfig().getString("arena.countdown-second");
                            arena.broadcast(
                                    message.replace("%time%", "§c" + time)
                                            .replace("&", "§")
                            );
                        }
                        else if (time <= 0) {
                            arena.setState(ArenaState.PHASE_1);
                        }
                    }
                }, 0L, 20L);
    }
    public void phase_1() {

    }
    public void phase_2() {
    }
    public void phase_3() {
    }

    public void doom() {
    }

    public void ending() {

    }
}
