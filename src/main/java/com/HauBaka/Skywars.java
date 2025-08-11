package com.HauBaka;

import co.aikar.commands.PaperCommandManager;
import com.HauBaka.modules.command.testCommand;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public class Skywars extends JavaPlugin {
    @Getter
    private static final Logger logger = LogManager.getLogger("Skywars");

    @Getter
    private static Skywars instance;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("Plugin enabled!");

        registerCommands();

    }

    @Override
    public void onDisable() {

    }

    private void registerCommands() {
        PaperCommandManager manager = new PaperCommandManager(this);
        manager.registerCommand(new testCommand());
    }
}
