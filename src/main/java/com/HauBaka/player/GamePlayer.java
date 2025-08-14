package com.HauBaka.player;

import com.HauBaka.object.GameScoreboard;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Map;

public class GamePlayer {
    private static Map<Player, GamePlayer> gamePlayers;
    @Getter
    private Player player;
    @Getter
    private GameScoreboard scoreboard;

    public static GamePlayer getGamePlayer(Player player) {
        return gamePlayers.getOrDefault(player, null);
    }
}
