package com.HauBaka.arena;

import com.HauBaka.object.ArenaChest;
import com.HauBaka.enums.ArenaState;
import com.HauBaka.enums.ArenaVariant;
import com.HauBaka.event.ArenaStageChangeEvent;
import com.HauBaka.player.GamePlayer;
import com.HauBaka.world.WorldManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.List;

public class Arena {
    @Getter
    private final ArenaVariant variant;
    @Getter
    private ArenaState state;
    @Getter
    private List<GamePlayer> players;
    @Getter
    private List<ArenaTeam> teams;
    @Getter
    private final String name;
    @Getter
    private final String id;
    @Getter
    private ArenaCountdownTask countDownTask;
    @Getter
    private final TemplateArena templateArena;
    @Getter
    private World world;
    @Getter
    private List<ArenaChest> midChests;


    public Arena(TemplateArena templateArena, ArenaVariant variant) {
        this.variant = variant;
        this.name = templateArena.getName();
        this.id = ArenaManager.generateID();
        this.templateArena = templateArena;
        create();
    }

    private void create() {
        WorldManager.cloneWorld(templateArena.getMapName(), ArenaManager.generateID(), w -> {
            this.world = w;
            templateArena.clone(this);
        });
    }

    public boolean addPlayer(GamePlayer gamePlayer) {
        if (!(state == ArenaState.AVAILABLE || state == ArenaState.WAITING || state == ArenaState.STARTING) ||
                players.size() == variant.getMode().getAmountPlayer() ||
                players.contains(gamePlayer)
        ) return false;

        for (ArenaTeam team : teams) {
            if (team.addPlayer(gamePlayer)) {
                players.add(gamePlayer);
                if (state == ArenaState.AVAILABLE) setState(ArenaState.WAITING);
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
        if (this.state == ArenaState.WAITING) {
            countDownTask.waiting();
        } else if (this.state == ArenaState.STARTING) {
            countDownTask.starting();
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
        for (GamePlayer gamePlayer : players) {
            gamePlayer.getPlayer().sendMessage(s);
        }
    }
}
