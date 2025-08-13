package com.HauBaka;

import co.aikar.commands.PaperCommandManager;
import com.HauBaka.modules.command.testCommand;
import com.HauBaka.modules.file.FileConfig;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Skywars extends JavaPlugin {
    @Getter
    private static final Logger logger = LogManager.getLogger("Skywars");
    @Getter
    private FileConfig messageConfig;
    @Getter
    private static Skywars instance;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("Plugin enabled!");
        this.messageConfig = new FileConfig("message.yml");
        this.messageConfig.saveDefaultConfig();
        registerCommands();
        registerEvents();
    }

    private void registerEvents() {
        PluginManager pm = Bukkit.getPluginManager();
        //pm.registerEvents(new ArenaListener(), this);

    }

    @Override
    public void onDisable() {

    }

    private void registerCommands() {
        PaperCommandManager manager = new PaperCommandManager(this);
        manager.registerCommand(new testCommand());
    }
}
