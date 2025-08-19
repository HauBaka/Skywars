package com.HauBaka.arena;

import com.HauBaka.Skywars;
import com.HauBaka.event.PlayerDamagePlayerEvent;
import com.HauBaka.event.PlayerDeathEvent;
import com.HauBaka.object.ArenaChest;
import com.HauBaka.enums.ArenaState;
import com.HauBaka.enums.ArenaVariant;
import com.HauBaka.event.ArenaStageChangeEvent;
import com.HauBaka.player.GamePlayer;
import com.HauBaka.world.WorldManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Arena implements Listener {
    @Getter
    private final ArenaVariant variant;
    @Getter
    private ArenaState state;
    @Getter
    private final List<GamePlayer> players;
    @Getter
    private final List<GamePlayer> alive_players;
    @Getter
    private final List<GamePlayer> spectators;
    @Getter
    private final Map<GamePlayer, Integer> kills;
    @Getter
    private final Map<GamePlayer, Integer> assists;
    @Getter
    private final List<ArenaTeam> teams;
    @Getter
    private final List<ArenaTeam> alive_teams;
    @Getter
    private final String name;
    @Getter
    private final String id;
    @Getter
    private final ArenaCountdownTask countDownTask;
    @Getter
    private final TemplateArena templateArena;
    @Getter
    private World world;
    @Getter
    private final List<ArenaChest> midChests;
    @Getter @Setter
    private Location lobby;
    @Getter @Setter
    private int time;
    public Arena(TemplateArena templateArena, ArenaVariant variant) {
        this.variant = variant;
        this.name = templateArena.getName();
        this.id = ArenaManager.generateID();
        this.templateArena = templateArena;
        this.countDownTask = new ArenaCountdownTask(this);
        this.state = ArenaState.LOADING;
        this.players = new ArrayList<>();
        this.alive_players = new ArrayList<>();
        this.spectators = new ArrayList<>();
        this.kills = new LinkedHashMap<>();
        this.assists = new LinkedHashMap<>();
        this.teams = new ArrayList<>();
        this.alive_teams = new ArrayList<>();
        this.midChests = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(this, Skywars.getInstance());
        create();
    }

    private void create() {
        WorldManager.cloneWorld(templateArena.getMapName(), id, w -> {
            this.world = w;
            templateArena.setUp(this);
            setState(ArenaState.AVAILABLE);
        });
    }
    public boolean addPlayer(GamePlayer gamePlayer) {
        gamePlayer.sendMessage("");
        gamePlayer.sendMessage("&6&lINFO&r&e Sending you to &a" + world.getName() +"&e...");
        if (!(state == ArenaState.AVAILABLE || state == ArenaState.WAITING || state == ArenaState.STARTING) ||
                players.size() == variant.getMode().getMaxPlayer() ||
                players.contains(gamePlayer)
        ) {
            gamePlayer.sendMessage("&4&lERROR!&r&c You had already been in this game!");
            return false;
        }

        for (ArenaTeam team : teams) {
            if (team.addPlayer(gamePlayer)) {
                if (state == ArenaState.AVAILABLE) setState(ArenaState.WAITING);
                if (players.size() == variant.getMode().getMinPlayer() && state == ArenaState.WAITING) {
                    setState(ArenaState.STARTING);
                }
                return true;
            }
        }

        return false;
    }
    public void setState(ArenaState state) {
        ArenaState oldState = this.state;
        this.state = state;
        Bukkit.getPluginManager().callEvent(new ArenaStageChangeEvent(this, oldState, state));

        countDownTask.cancelTask();
        setTime(getState().getTime());
        if (this.state == ArenaState.WAITING) {
            countDownTask.waiting();
        } else if (this.state == ArenaState.STARTING) {
            countDownTask.starting();
        } else if (this.state == ArenaState.CAGE_OPENING) {
            alive_players.addAll(players);
            alive_teams.addAll(teams);
            for (GamePlayer gamePlayer : players) {
                kills.put(gamePlayer, 0);
                assists.put(gamePlayer, 0);
            }
            countDownTask.cage_opening();
        } else if (this.state == ArenaState.PHASE_1) {
            countDownTask.phase_1();
        } else if (this.state == ArenaState.PHASE_2) {
            countDownTask.phase_2();
        } else if (this.state == ArenaState.PHASE_3) {
            countDownTask.phase_3();
        } else if (this.state == ArenaState.DOOM) {
            countDownTask.doom();
        } else if (this.state == ArenaState.ENDING) {
            countDownTask.ending();
        }
    }
    public void refill() {
        for (ArenaTeam team : teams) team.refill();
        for (ArenaChest midChest : midChests) midChest.refill();
    }
    public void broadcast(String s) {
        if (s == null) return;
        if (state == ArenaState.AVAILABLE || state == ArenaState.WAITING || state == ArenaState.STARTING) {
            for (GamePlayer gamePlayer : players) {
                gamePlayer.sendMessage(s);
            }
            return;
        }
        for (GamePlayer gamePlayer : alive_players) {
            gamePlayer.sendMessage(s);
        }
        for (GamePlayer gamePlayer : spectators) {
            gamePlayer.sendMessage(s);
        }
    }
    public void removeLobby() {
        for (int x = -12; x <= 12; ++x) {
            for (int z = -12; z<=12; ++z) {
                for (int y = -3; y <=7; ++y) {
                    lobby.clone().add(x,y,z).getBlock().setType(Material.AIR);
                }
            }
        }
    }
    public void destroy() {
        for (ArenaTeam team : teams)
            for (ArenaChest chest : team.getSpawnChests())
                chest.destroy();
        for (ArenaChest chest : midChests)
            chest.destroy();
        ArenaManager.removeArena(id);
        HandlerList.unregisterAll(this);
        WorldManager.removeWorld(id);
    }

    @EventHandler
    public void blockBreakEvent(BlockBreakEvent event) {
        if (event.getPlayer().getWorld() != world) return;
        Bukkit.getPluginManager().callEvent(new com.HauBaka.event.BlockBreakEvent(
                this, GamePlayer.getGamePlayer(event.getPlayer()), event));
    }
    @EventHandler
    public void interactEvent(PlayerInteractEvent event) {
        if (event.getPlayer().getWorld() != world) return;
        Bukkit.getPluginManager().callEvent(new com.HauBaka.event.PlayerInteractEvent(
                this, GamePlayer.getGamePlayer(event.getPlayer()),event));
    }
    @EventHandler
    public void entityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (
                !(event.getEntity() instanceof Player) ||
                !(event.getDamager() instanceof Player) ||
                event.getEntity().getWorld() != world
        ) return;
        GamePlayer victim = GamePlayer.get((Player) event.getEntity());
        GamePlayer attacker = GamePlayer.get((Player) event.getDamager());
        Bukkit.getPluginManager().callEvent(new PlayerDamagePlayerEvent(this, victim, attacker, event));
    }
    @EventHandler
    public void entityDeathEvent(org.bukkit.event.entity.PlayerDeathEvent event) {
        if (
                event.getEntity().getWorld() != world
        ) return;
        GamePlayer victim = GamePlayer.get((Player) event.getEntity());
        GamePlayer attacker = GamePlayer.get((Player) event.getEntity().getKiller());
        Bukkit.getPluginManager().callEvent(new PlayerDeathEvent(this, victim, attacker, event));
    }
}
