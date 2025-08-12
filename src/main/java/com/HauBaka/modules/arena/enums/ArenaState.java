package com.HauBaka.modules.arena.enums;

import lombok.Getter;

public enum ArenaState {
    AVAILABLE("Available", -1),
    WAITING("Waiting", -1),
    STARTING("Starting", 15),
    PHASE_1("Phase 1", 180),
    PHASE_2("Phases 2", 120),
    PHASE_3("Phase 3", 150),
    DOOM("Phase 4", 150),
    ENDING("Ending", 12);
    @Getter
    private String name;
    @Getter
    private int time;
    ArenaState(String name, int time) {
        this.name = name;
        this.time = time;
    }
}
