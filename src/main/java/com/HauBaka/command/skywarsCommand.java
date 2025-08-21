package com.HauBaka.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.HauBaka.arena.Arena;
import com.HauBaka.arena.ArenaManager;
import com.HauBaka.player.GamePlayer;
import com.HauBaka.utils.ChatUtils;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

@CommandAlias("sw|skywars")
@Description("Available commands for skywars")
public class skywarsCommand extends BaseCommand {
    private static class CMD {
        String command, label, description;
        private CMD(String command, String label, String description) {
            this.command = command;
            this.label = label;
            this.description = description;
        }
    }
    private static final List<CMD> cmds = Arrays.asList(
            new CMD("/sw detail", "/skywars detail", "View more about this plugin"),
            new CMD("/sw help", "/sw help", "View available commands"),
            new CMD("/sw join ", "/sw join <arena>", "Join a SkyWars arena"),
            new CMD("/sw leave", "/sw leave", "Leave current arena")
    );

    @HelpCommand
    public static void onHelp(CommandSender sender) {
        for (CMD cmd : cmds) {
            ChatUtils.sendComplexMessage(sender,
                    ChatUtils.suggest("&b" + cmd.label, cmd.command,"&eClick to execute!"),
                    ChatUtils.simple(" &8- &e" + cmd.description)
            );
        }
    }
    @Subcommand("author|detail|details")
    public static void onDetail(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("");
        ChatUtils.sendComplexMessage(sender,
                ChatUtils.simple("&b&lSkywars plugin &r&8-&e vtest")
        );
        sender.sendMessage("");
        ChatUtils.sendComplexMessage(sender,
                ChatUtils.simple("&d&lAuthor information")
        );
        ChatUtils.sendComplexMessage(sender,
                ChatUtils.simple("Author: "),
                ChatUtils.link("&a&nHauBaka", "https://haubaka.xyz", "Open &aprofile&f page"),
                ChatUtils.simple(" • "),
                ChatUtils.link("&dGithub", "https://github.com/haubaka","Open &dgithub&f link"),
                ChatUtils.simple(" &8| "),
                ChatUtils.link("&3Facebook", "https://www.facebook.com/abcdefghiklmnopqrstuvxyz1234567890", "Open&3 facebook&f link")
        );
        sender.sendMessage("");
        sender.sendMessage("");
        ChatUtils.sendComplexMessage(sender,
                ChatUtils.simple("&d&lProject information")
        );
        ChatUtils.sendComplexMessage(sender,
                ChatUtils.simple("A simple "),
                ChatUtils.link("&b&nSkyWars", "https://hypixel.fandom.com/wiki/SkyWars", "&fClick to &eview more"),
                ChatUtils.simple(" "),
                ChatUtils.link("&d&nproject","https://github.com/HauBaka/Skywars", "Click to &eview project on &dgithub"),
                ChatUtils.simple(" created as a personal learning exercise to practice "),
                ChatUtils.link("&6&nJava programming","https://en.wikipedia.org/wiki/Java_(programming_language)", "Click to &eview more"),
                ChatUtils.simple(" and explore "),
                ChatUtils.link("&6&nSpigot", "https://www.spigotmc.org", "Click to &eview more"),
                ChatUtils.simple(" "),
                ChatUtils.link("&e&nplugin development", "https://www.spigotmc.org/wiki/spigot-plugin-development", "Click to &eview more")
        );
        sender.sendMessage("");
        ChatUtils.sendComplexMessage(sender,
                ChatUtils.simple("&4&lWarning! &r&cThis plugin is purely non-commercial, experimental, and intended for educational purposes only.")
        );
        sender.sendMessage("");
        ChatUtils.sendComplexMessage(sender,
                ChatUtils.simple("&fUsage: "),
                ChatUtils.command("&b/skywars help", "/skywars help", "Click to execute command"),
                ChatUtils.simple("&8 - &eTo view more available &bSkywars&e commands!")
        );
        sender.sendMessage("");
    }
    @Subcommand("join")
    @Syntax("<+tag> <arenaID>")
    @Description("Join a arena.")
    public static void onJoin(GamePlayer gamePlayer, String id) {
        Arena arena = ArenaManager.getByID(id);
        if (arena == null) {
            gamePlayer.sendMessage("&4&lERROR!&r&c No arena found!");
            return;
        }
        Arena oldArena = gamePlayer.getArena();
        if (oldArena != null) oldArena.removePlayer(gamePlayer);
        if (!arena.addPlayer(gamePlayer)) {
            gamePlayer.sendMessage("&4&lERROR!&r&c Can't join this arena!");
            return;
        }
    }
}
