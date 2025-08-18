package com.HauBaka.enums;
import lombok.Getter;

public enum ArenaState {
    LOADING("Loading", -1, null),
    AVAILABLE("Available", -1, null),
    WAITING("Waiting", -1, null),
    STARTING("Starting", 15, null),
    CAGE_OPENING("Cage opening", 6, null),
    PHASE_1("Refill", 180, null),
    PHASE_2("Refill", 120, null),
    PHASE_3("Refill", 150, null),
    DOOM("Doom", 150, null),
    ENDING("Ending", 12, null),
    TO_HUB("To Hub", 0, null);

    @Getter
    private ArenaState next;
    @Getter
    private final String name;
    @Getter
    private final int time;

    ArenaState(String name, int time, ArenaState next) {
        this.name = name;
        this.time = time;
        this.next = next;
    }

    static {
        LOADING.next = AVAILABLE;
        AVAILABLE.next = WAITING;
        WAITING.next = STARTING;
        STARTING.next = CAGE_OPENING;
        CAGE_OPENING.next = PHASE_1;
        PHASE_1.next = PHASE_2;
        PHASE_2.next = PHASE_3;
        PHASE_3.next = DOOM;
        DOOM.next = ENDING;
        ENDING.next = TO_HUB;
    }
}
