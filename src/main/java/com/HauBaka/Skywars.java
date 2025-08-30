package com.HauBaka;

import co.aikar.commands.PaperCommandManager;
import com.HauBaka.arena.ArenaManager;
import com.HauBaka.arena.setup.ArenaSetupManager;
import com.HauBaka.command.adminCommand;
import com.HauBaka.command.arenaSetupCommand;
import com.HauBaka.command.skywarsCommand;
import com.HauBaka.command.testCommand;
import com.HauBaka.file.FileConfig;
import com.HauBaka.handle.*;
import com.HauBaka.lobby.Lobby;
import com.HauBaka.npc.NPCManager;
import com.HauBaka.object.cage.CageManager;
import com.HauBaka.player.GamePlayer;
import com.HauBaka.world.WorldManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


public class Skywars extends JavaPlugin {
    @Getter
    private ProtocolManager protocolManager;

    @Getter
    private static FileConfig messageConfig;
    @Getter
    private static FileConfig configConfig;
    @Getter
    private static Skywars instance;
    @Override
    public void onEnable() {
        instance = this;
        protocolManager = ProtocolLibrary.getProtocolManager();
        messageConfig = new FileConfig("messages.yml");
        messageConfig.saveDefaultConfig();
        configConfig = new FileConfig("config.yml");
        configConfig.saveDefaultConfig();
        NPCManager.init();
        CageManager.init();
        WorldManager.init();
        ArenaManager.init();
        ArenaSetupManager.init();
        GamePlayer.init();
        Lobby.init();
        registerCommands();
        registerEvents();
        WorldManager.disableWorldLogs();
    }

    private void registerEvents() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new blockBreakHandle(), this);
        pm.registerEvents(new chestHandle(), this);
        pm.registerEvents(new stageChangeHandle(), this);
        pm.registerEvents(new playerDamageHandle(), this);
        pm.registerEvents(new playerDeathHandle(), this);
        pm.registerEvents(new Lobby(), this);

    }

    @Override
    public void onDisable() {

    }

    private void registerCommands() {
        PaperCommandManager manager = new PaperCommandManager(this);

        manager.getCommandContexts().registerContext(GamePlayer.class, c ->
                GamePlayer.getGamePlayer(c.getPlayer())
        );

        manager.registerCommand(new testCommand());
        manager.registerCommand(new skywarsCommand());
        manager.registerCommand(new arenaSetupCommand());
        manager.registerCommand(new adminCommand());
    }
}
