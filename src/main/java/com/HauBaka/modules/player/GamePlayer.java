package com.HauBaka.modules.player;

import com.HauBaka.modules.object.GameScoreboard;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Map;

public class GamePlayer {
    @Getter
    private static Map<Player, GamePlayer> gamePlayers;
    @Getter
    private Player player;
    @Getter
    private GameScoreboard scoreboard;

}
