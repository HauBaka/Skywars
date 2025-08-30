package com.HauBaka.player;

import com.HauBaka.enums.ArenaVariant;
import com.HauBaka.object.cage.Cage;
import com.HauBaka.object.cage.CageManager;
import lombok.Getter;

import java.util.*;

@Getter
public class PlayerData {
    private enum DataVariable {
        KILLS, WINS, ASSISTS, BLOCK_PLACED
    }
    private int
            coins,
            souls,
            max_souls;
    private double experience;
    private Cage selectedCage;
    private List<Cage> unlockedCages;
    private Map<ArenaVariant, Map<DataVariable, Integer>> stats;
    public PlayerData(UUID uuid) {
        stats = new HashMap<>();
        unlockedCages = new ArrayList<>();
        selectedCage = CageManager.getCage("default");
    }
}
