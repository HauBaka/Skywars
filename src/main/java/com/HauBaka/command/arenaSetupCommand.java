package com.HauBaka.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.HauBaka.arena.setup.ArenaSetupManager;
import com.HauBaka.player.GamePlayer;
import com.HauBaka.world.WorldManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("sw|skywars")
public class arenaSetupCommand extends BaseCommand {
    @HelpCommand
    public static void onHelp(Player player) {
        player.sendMessage("Coming soon");
    }
    @Subcommand("setup")
    @Syntax("<+tag> [worldName]")
    @Description("Create a map and start setting up it.")
    @CommandPermission("skywars.admin")
    public static void onSetUp(Player player, String worldName) {
        if (!WorldManager.checkExistWorldName(worldName)) {
            player.sendMessage(
                    ChatColor.translateAlternateColorCodes(
                            '&',
                            "&4&lERROR!&r&c " + worldName +".slime file does not exist!"
                    )
            );
            return;
        }
        ArenaSetupManager.createEdit(worldName, GamePlayer.getGamePlayer(player));
    }
}
