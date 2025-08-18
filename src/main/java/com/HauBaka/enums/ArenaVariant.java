package com.HauBaka.enums;

import lombok.Getter;

public class ArenaVariant {
    public enum Mode {
        SOLO(12,1, 2),
        DOUBLES(24,2, 2),
        MEGA(60,5, 2);

        @Getter
        final
        int maxPlayer;
        @Getter
        final int playerPerTeam;
        @Getter
        int minPlayer;
        Mode(int maxPlayer, int playerPerTeam, int minPlayer) {
            this.maxPlayer = maxPlayer;
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
