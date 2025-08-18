package com.HauBaka.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Subcommand;
import com.HauBaka.utils.ChatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("sw|skywars")
public class skywarsCommand extends BaseCommand {
    @HelpCommand
    public static void onHelp(Player player) {
        //soon
    }
    @Subcommand("author|detail")
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

}
