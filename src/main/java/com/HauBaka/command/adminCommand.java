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
import com.HauBaka.lobby.Lobby;
import com.HauBaka.npc.NPC;
import com.HauBaka.npc.NPCManager;
import com.HauBaka.npc.NPCType;
import com.HauBaka.player.GamePlayer;
import com.HauBaka.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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

        Arena arena = ArenaManager.createArena(templateArena, ArenaVariant.fromKey(mode, type));
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
    @Syntax("<arenaID>")
    public static void onForceStart(GamePlayer gamePlayer, String arenaID) {
        Arena arena = arenaID.isEmpty() ? gamePlayer.getArena() : ArenaManager.getByID(arenaID);
        if (arena == null) {
            gamePlayer.sendMessage("&4&lERROR!&r&c No game found!");
            return;
        }
        if (arena.getState() == ArenaState.AVAILABLE || arena.getState() == ArenaState.WAITING) {
            arena.broadcast("&eThis game has been forced to start!");
            arena.setState(ArenaState.CAGE_OPENING);
        } else gamePlayer.sendMessage("&4&lERROR!&r&c This game started!");

    }


    @Subcommand("lobby")
    @CommandPermission("sw.admin.lobby")
    @Syntax("<setspawn/source>")
    public static void onSetLobby(GamePlayer gamePlayer, String[] args) {
        if (args.length < 1) return;
        switch (args[0].toLowerCase()) {
            case "source":
                if (args.length < 2) return;
                StringBuilder source = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    source.append(args[i]).append(" ");
                }
                Lobby.setSource(source.toString());
                break;
            case "setspawn":
                Lobby.setLocation(gamePlayer.getPlayer().getLocation());
                break;
            default:
                gamePlayer.sendMessage("&4&lERROR!&r&c Invalid command!");
        }
    }
    @Subcommand("npc")
    @CommandPermission("sw.admin.npc")
    @Syntax("<add/remove> <...>|e")
    public static void onNPCSetup(GamePlayer gamePlayer, String[] args) {
        if (args.length == 0) return;
        switch (args[0].toLowerCase()) {
            case "add":
                if (args.length < 4) {
                    return;
                }

                NPCType type;
                try {
                    type = NPCType.valueOf(args[1].toUpperCase());
                } catch (IllegalArgumentException ignored) {
                    gamePlayer.sendMessage("&4&lERROR! &cInvalid NPC type!");
                    return;
                }

                String skinName = args[2];
                StringBuilder cmd = new StringBuilder();
                for (int i = 3 ; i < args.length ; i++) cmd.append(args[i]).append(" ");

                NPC npc = NPCManager.createNPC(gamePlayer.getPlayer().getLocation(), type);
                npc.setSkin(skinName);
                npc.setClickEvent(p -> p.performCommand(cmd.toString()));
                npc.setCmd(cmd.toString());

                Lobby.addNPC(npc);
                gamePlayer.sendMessage("&2&lADDED!&r&a Created a NPC named &e" + npc.getName());
                break;
            case "remove":
                if (args.length != 2) return;

                if (Lobby.removeNPC(NPCManager.getNPCFromName(args[1]))) {
                    gamePlayer.sendMessage("&2&lREMOVED!&r&a Removed NPC named &e" + args[1]);
                } else gamePlayer.sendMessage("&4&lERROR!&r&c NPC does not exist!");
                break;
            default:
                break;
        }
    }
}
