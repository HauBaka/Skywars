package com.HauBaka.arena;

import com.HauBaka.Skywars;
import com.HauBaka.arena.spectator.SpectatorItems;
import com.HauBaka.event.PlayerDamagePlayerEvent;
import com.HauBaka.event.PlayerDeathEvent;
import com.HauBaka.object.ArenaChest;
import com.HauBaka.enums.ArenaState;
import com.HauBaka.enums.ArenaVariant;
import com.HauBaka.event.ArenaStageChangeEvent;
import com.HauBaka.player.GamePlayer;
import com.HauBaka.utils.ChatUtils;
import com.HauBaka.world.WorldManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

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
    @Getter
    private final Set<ArenaChest> openedChests;
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
        this.openedChests = new HashSet<>();
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
        if (state == ArenaState.AVAILABLE || state == ArenaState.WAITING || state == ArenaState.STARTING) {
            if (players.size() == variant.getMode().getMaxPlayer()) {
                gamePlayer.sendMessage("&4&lERROR!&r&c This game reached max players!");
                return false;
            }
            else if (players.contains(gamePlayer)) {
                gamePlayer.sendMessage("&4&lERROR!&r&c You had already been in this game!");
                return false;
            }
            for (ArenaTeam team : teams) {
                if (team.addPlayer(gamePlayer)) {
                    healPlayer(gamePlayer);
                    if (state == ArenaState.AVAILABLE) setState(ArenaState.WAITING);
                    if (players.size() == variant.getMode().getMinPlayer() && state == ArenaState.WAITING) {
                        setState(ArenaState.STARTING);
                    }
                    return true;
                }
            }
            gamePlayer.sendMessage("&4&lERROR!&r&c Can't find a team for you!");
            return false;
        }

        return addSpectator(gamePlayer);
    }

    public boolean addSpectator(GamePlayer gamePlayer) {
        if (spectators.contains(gamePlayer)) {
            gamePlayer.sendMessage("&4&lERROR!&r&c You had already been in this game!");
            return false;
        }
        Arena oldArena = gamePlayer.getArena();
        if (oldArena != null && oldArena != this) oldArena.removePlayer(gamePlayer);

        ArenaTeam team = getTeam(gamePlayer);
        new BukkitRunnable() {
            @Override
            public void run() {
                healPlayer(gamePlayer);

                Player player = gamePlayer.getPlayer();
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100000000, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100000000, 0));
                player.teleport(team != null ? team.getSpawnLocation() : lobby);
                player.setAllowFlight(true);
                player.setFlying(true);

                SpectatorItems.teleportCompass(gamePlayer, Arena.this,0);
                SpectatorItems.settings(gamePlayer, Arena.this,4);
                SpectatorItems.playAgain(gamePlayer, Arena.this,7);
                SpectatorItems.returnToLobby(gamePlayer, Arena.this,8);
            }
        }.runTaskLater(Skywars.getInstance(), 5L);

        spectators.add(gamePlayer);

        return true;
    }
    public void removePlayer(GamePlayer gamePlayer) {
        if (state == ArenaState.AVAILABLE || state == ArenaState.WAITING || state == ArenaState.STARTING) {
            if (!players.contains(gamePlayer)) return;
            ArenaTeam team = getTeam(gamePlayer);
            if (team != null) {
                team.removePlayer(gamePlayer);
                broadcast(gamePlayer.getPlayer().getDisplayName() + " quit!");
            }
        }
        alive_players.remove(gamePlayer);
        spectators.remove(gamePlayer);
    }
    public void setState(ArenaState state) {
        ArenaState oldState = this.state;
        this.state = state;
        countDownTask.cancelTask();
        setTime(state.getTime());
        Bukkit.getPluginManager().callEvent(new ArenaStageChangeEvent(this, oldState, state));
    }
    public void refill() {
        openedChests.clear();
        for (ArenaTeam team : teams) team.refill();
        for (ArenaChest midChest : midChests) midChest.refill();
    }
    public void broadcast(String s) {
        if (s == null || s.isEmpty()) return;
        if (state == ArenaState.AVAILABLE || state == ArenaState.WAITING || state == ArenaState.STARTING) {
            for (GamePlayer gamePlayer : players) gamePlayer.sendMessage(s);
            return;
        }
        for (GamePlayer gamePlayer : alive_players) gamePlayer.sendMessage(s);
        for (GamePlayer gamePlayer : spectators) gamePlayer.sendMessage(s);
    }
    public void removeLobby() {
        for (int x = -12; x <= 12; ++x)
            for (int z = -12; z<=12; ++z)
                for (int y = -3; y <=7; ++y) lobby.clone().add(x,y,z).getBlock().setType(Material.AIR);
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
    public ArenaTeam getTeam(GamePlayer gamePlayer) {
        for (ArenaTeam team : teams)
            if (team.getMembers().contains(gamePlayer))
                return team;
        return null;
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
        if (!alive_players.contains(GamePlayer.getGamePlayer(event.getPlayer()))) event.setCancelled(true);
        Bukkit.getPluginManager().callEvent(new com.HauBaka.event.PlayerInteractEvent(
                this, GamePlayer.getGamePlayer(event.getPlayer()), event));
    }
    @EventHandler
    public void entityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (
                !(event.getEntity() instanceof Player) ||
                !(event.getDamager() instanceof Player) ||
                event.getEntity().getWorld() != world
        ) return;
        GamePlayer victim = GamePlayer.getGamePlayer((Player) event.getEntity());
        GamePlayer attacker = GamePlayer.getGamePlayer((Player) event.getDamager());
        Bukkit.getPluginManager().callEvent(new PlayerDamagePlayerEvent(this, victim, attacker, event));
    }
    @EventHandler
    public void entityDeathEvent(org.bukkit.event.entity.PlayerDeathEvent event) {
        if (
                event.getEntity().getWorld() != world
        ) return;
        GamePlayer victim = GamePlayer.getGamePlayer((Player) event.getEntity());
        GamePlayer attacker = GamePlayer.getGamePlayer((Player) event.getEntity().getKiller());
        event.setDeathMessage(null);
        alive_players.remove(victim);
        addSpectator(victim);
        ChatUtils.sendComplexMessage(
                victim.getPlayer(),
                ChatUtils.simple("&cYou died!&e Want to play again? "),
                ChatUtils.command(
                        "&b&lClick here!",
                        "/play " + getVariant().toString(),
                        "Click here to play another game of &bSkywars"
                )
        );
        broadcast(victim.getPlayer().getDisplayName() +" died!");


        Bukkit.getPluginManager().callEvent(new PlayerDeathEvent(this, victim, attacker, event));
    }

    private void healPlayer(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();
        ((CraftPlayer)player).getHandle().getDataWatcher().watch(9, (byte) 0);
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setMaxHealth(20.0);
        player.setHealth(20.0);
        player.setLevel(0);
        player.setExp(0.0f);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setHeldItemSlot(0);
        player.updateInventory();
        for (PotionEffect effect: player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }
}
