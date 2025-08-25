package com.HauBaka.enums;

import com.HauBaka.utils.Utils;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.bukkit.Bukkit;

public enum ArenaVariant {
    SOLO_NORMAL(Mode.SOLO, Type.NORMAL),
    SOLO_INSANE(Mode.SOLO, Type.INSANE),

    DOUBLES_NORMAL(Mode.DOUBLES, Type.NORMAL),
    DOUBLES_INSANE(Mode.DOUBLES, Type.INSANE),

    MEGA_NORMAL(Mode.MEGA, Type.NORMAL),
    MEGA_INSANE(Mode.MEGA, Type.INSANE);

    private final Mode mode;
    private final Type type;

    ArenaVariant(Mode mode, Type type) {
        this.mode = mode;
        this.type = type;
    }

    public Mode getMode() {
        return mode;
    }

    public Type getType() {
        return type;
    }

    public String getKey() {
        return name();
    }

    @Override
    public String toString() {
        return type.toString() + " " + Utils.toBetterName(mode.name());
    }

    public static ArenaVariant fromKey(Mode mode, Type type) {
        for (ArenaVariant variant : ArenaVariant.values()) {
            if (variant.mode.equals(mode) && variant.type.equals(type)) {
                return variant;
            }
        }
        return null;
    }
    public static ArenaVariant fromKey(String key) {
        if (key == null) return null;
        String normalized = key.trim().toUpperCase()
                .replace('-', '_')
                .replaceAll("\\s+", "_");
        try {
            return ArenaVariant.valueOf(normalized);
        } catch (IllegalArgumentException ignored) { }

        String[] parts = normalized.split("_");
        if (parts.length >= 2) {
            String modePart = parts[0];
            String typePart = parts[parts.length - 1];
            try {
                Mode m = Mode.valueOf(modePart);
                Type t = Type.valueOf(typePart);
                for (ArenaVariant av : values()) {
                    if (av.mode == m && av.type == t) return av;
                }
            } catch (IllegalArgumentException ignored) { }
        }

        Bukkit.getLogger().warning("Unknown ArenaVariant: " + key);
        return null;
    }

    public enum Mode {
        SOLO(12, 1, 2),
        DOUBLES(24, 2, 2),
        MEGA(60, 5, 2);

        private final int maxPlayer;
        private final int playerPerTeam;
        private final int minPlayer;

        Mode(int maxPlayer, int playerPerTeam, int minPlayer) {
            this.maxPlayer = maxPlayer;
            this.playerPerTeam = playerPerTeam;
            this.minPlayer = minPlayer;
        }

        public int getMaxPlayer() {
            return maxPlayer;
        }

        public int getPlayerPerTeam() {
            return playerPerTeam;
        }

        public int getMinPlayer() {
            return minPlayer;
        }
    }

    public enum Type {
        NORMAL("§a"),
        INSANE("§c");

        private final String color;

        Type(String color) {
            this.color = color;
        }

        public String getColor() {
            return color;
        }

        @Override
        public String toString() {
            return this.color + Utils.toBetterName(name());
        }
    }
}
