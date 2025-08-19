package com.HauBaka.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("test")
public class testCommand extends BaseCommand {
    @HelpCommand
    public void onHelp(CommandSender sender) {
        sender.sendMessage("stfu");
    }

    @Subcommand("removeLobby")
    public void helpSw(Player sender) {
        Location loc = sender.getLocation();
        //removeLobby(loc);
    }


}
