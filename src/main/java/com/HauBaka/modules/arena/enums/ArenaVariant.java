package com.HauBaka.modules.arena.enums;

import lombok.Getter;

public class ArenaVariant {
    public enum Mode {
        SOLO(12,1, 2), DOUBLES(24,2, 2), MEGA(100,5, 2);

        @Getter
        final
        int amountPlayer;
        @Getter
        final int playerPerTeam;
        @Getter
        int minPlayer;
        Mode(int amountPlayer, int playerPerTeam, int minPlayer) {
            this.amountPlayer = amountPlayer;
            this.playerPerTeam = playerPerTeam;
        }
    }
    public enum Type {
        NORMAL,
        INSANE
    }
    @Getter
    private final Mode mode;
    @Getter
    private final Type type;

    ArenaVariant(Mode mode, Type type) {
        this.mode = mode;
        this.type = type;
    }
}
