package com.HauBaka.player;

import com.HauBaka.arena.Arena;
import com.HauBaka.object.GameScoreboard;
import com.HauBaka.object.cage.Cage;
import com.HauBaka.object.cage.CageManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
public class GamePlayer {
    private static Map<Player, GamePlayer> gamePlayers;
    private final Player player;
    private final GameScoreboard scoreboard;
    @Setter
    private Arena arena;
    private PlayerData playerData;
    private GamePlayer(Player player) {
        this.player = player;
        this.playerData = new PlayerData(player.getUniqueId());
        this.scoreboard = new GameScoreboard(this);
        //Test
        this.scoreboard.setContents(Arrays.asList(
                "§4♥ §cRed Love §4♥",
                "§6★ §eGolden Star §6★",
                "§2✔ §aGreen Check §2✔",
                "§1❖ §9Blue Diamond §1❖",
                "§5♫ §dPurple Music §5♫",
                "§3➤ §bCyan Arrow §3➤",
                "§f§lBold White Text",
                "§7§oGray Italic",
                "§8Shadow Text",
                "§e§nYellow Underline"
        ));
        this.scoreboard.show();
    }
    public static void init() {
        gamePlayers = new HashMap<>();
        for (Player p : Bukkit.getOnlinePlayers()) getGamePlayer(p);
    }
    public static GamePlayer getGamePlayer(Player player) {
        if (player == null) return null;
        if (gamePlayers.containsKey(player)) return gamePlayers.get(player);
        GamePlayer gamePlayer = new GamePlayer(player);
        gamePlayers.put(player, gamePlayer);
        return gamePlayer;
    }
    public void sendMessage(String message) {
        if (player != null && player.isOnline()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
    public void sendMessage(String... messages) {
        for (String message : messages) sendMessage(message);
    }
}
