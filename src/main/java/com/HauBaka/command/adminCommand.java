package com.HauBaka.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.HauBaka.Skywars;
import com.HauBaka.arena.Arena;
import com.HauBaka.arena.ArenaManager;
import com.HauBaka.arena.TemplateArena;
import com.HauBaka.enums.ArenaState;
import com.HauBaka.enums.ArenaVariant;
import com.HauBaka.player.GamePlayer;
import com.HauBaka.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@CommandAlias("sw|skywars")
public class adminCommand extends BaseCommand {
    @Subcommand("clone")
    @CommandPermission("sw.admin.clone")
    @Syntax("<+tag> <mapName> <mode> <type>")
    public static void onClone(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&cUsage: /skywars clone <mapName> <mode> <type>"));
            return;
        }

        ArenaVariant.Mode mode;
        ArenaVariant.Type type;

        try {
            mode = ArenaVariant.Mode.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid mode!");
            return;
        }

        try {
            type = ArenaVariant.Type.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid type!");
            return;
        }

        TemplateArena templateArena = new TemplateArena(args[0]);
        if (!templateArena.isValid()) {
            sender.sendMessage(ChatColor.RED + "Invalid template name!");
            return;
        }

        Arena arena = ArenaManager.createArena(templateArena, new ArenaVariant(mode, type));
        final int[] taskID = {0};

        taskID[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(Skywars.getInstance(), new Runnable() {
            int second = 0;

            @Override
            public void run() {
                if (arena.getWorld() != null) {
                    ChatUtils.sendComplexMessage(sender,
                            ChatUtils.simple("&2&lCREATED!&r&a " + args[0] + " &ehas been created successfully! ")
                    );
                    ChatUtils.sendComplexMessage(sender,
                            ChatUtils.command("&e&lClick here", "/sw join " + arena.getId(), "&eClick here to join!"),
                            ChatUtils.simple(" &eto join!")
                    );
                    Bukkit.getScheduler().cancelTask(taskID[0]);
                } else if (second >= 5) {
                    sender.sendMessage(ChatColor.RED + "Time out!");
                    Bukkit.getScheduler().cancelTask(taskID[0]);
                }
                second++;
            }
        }, 0L, 20L);
    }

    @Subcommand("fstart|forcestart|fs")
    @CommandPermission("sw.admin.fs")
    @Syntax("<+tag> <arenaID>")
    public static void onForceStart(GamePlayer gamePlayer, String arenaID) {
        Arena arena = arenaID.isEmpty() ? gamePlayer.getArena() : ArenaManager.getByID(arenaID);
        if (arena == null) {
            gamePlayer.sendMessage("&4&lERROR!&r&c No game found!");
            return;
        }
        if (arena.getState() == ArenaState.AVAILABLE || arena.getState() == ArenaState.WAITING) {
            arena.broadcast("&eThis game has been forced to start!");
            arena.setState(ArenaState.STARTING);
        } else gamePlayer.sendMessage("&4&lERROR!&r&c This game started!");

    }
}
