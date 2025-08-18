package com.HauBaka;

import co.aikar.commands.PaperCommandManager;
import com.HauBaka.arena.ArenaManager;
import com.HauBaka.arena.setup.ArenaSetupManager;
import com.HauBaka.command.arenaSetupCommand;
import com.HauBaka.command.skywarsCommand;
import com.HauBaka.command.testCommand;
import com.HauBaka.file.FileConfig;
import com.HauBaka.object.cage.CageManager;
import com.HauBaka.player.GamePlayer;
import com.HauBaka.world.WorldManager;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Skywars extends JavaPlugin {

    @Getter
    private static final Logger pluginLogger = LogManager.getLogger("Skywars");
    @Getter
    private FileConfig messageConfig;
    @Getter
    private FileConfig config;
    @Getter
    private static Skywars instance;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("Plugin enabled!");
        CageManager.init();
        WorldManager.init();
        ArenaManager.init();
        ArenaSetupManager.init();
        GamePlayer.init();
        this.messageConfig = new FileConfig("messages.yml");
        this.messageConfig.saveDefaultConfig();
        this.config = new FileConfig("config.yml");
        this.config.saveDefaultConfig();
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
        manager.registerCommand(new skywarsCommand());
        manager.registerCommand(new arenaSetupCommand());
    }
}
