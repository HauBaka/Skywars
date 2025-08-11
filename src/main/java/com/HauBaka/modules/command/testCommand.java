package com.HauBaka.modules.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("help")
public class testCommand extends BaseCommand {
    @HelpCommand
    public void onHelp(CommandSender sender) {
        sender.sendMessage("stfu");
    }

    @Subcommand("empty")
    public void onEmpty() {}

    @Subcommand("sw")
    public void helpSw(Player sender) {
        sender.sendMessage("test");
    }
}
