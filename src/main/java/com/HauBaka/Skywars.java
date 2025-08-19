package com.HauBaka;

import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.contexts.ContextResolver;
import com.HauBaka.arena.ArenaManager;
import com.HauBaka.arena.setup.ArenaSetupManager;
import com.HauBaka.command.adminCommand;
import com.HauBaka.command.arenaSetupCommand;
import com.HauBaka.command.skywarsCommand;
import com.HauBaka.command.testCommand;
import com.HauBaka.file.FileConfig;
import com.HauBaka.handle.blockBreak;
import com.HauBaka.object.cage.CageManager;
import com.HauBaka.player.GamePlayer;
import com.HauBaka.world.WorldManager;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Skywars extends JavaPlugin {

    @Getter
    private static final Logger pluginLogger = LogManager.getLogger("Skywars");
    @Getter
    private FileConfig messageConfig;
    @Getter
    private static FileConfig configConfig;
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
        configConfig = new FileConfig("config.yml");
        configConfig.saveDefaultConfig();
        registerCommands();
        registerEvents();
    }

    private void registerEvents() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new blockBreak(), this);

    }

    @Override
    public void onDisable() {

    }

    private void registerCommands() {
        PaperCommandManager manager = new PaperCommandManager(this);

        manager.getCommandContexts().registerContext(GamePlayer.class, c ->
                GamePlayer.get(c.getPlayer())
        );


        manager.registerCommand(new testCommand());
        manager.registerCommand(new skywarsCommand());
        manager.registerCommand(new arenaSetupCommand());
        manager.registerCommand(new adminCommand());
    }
}
