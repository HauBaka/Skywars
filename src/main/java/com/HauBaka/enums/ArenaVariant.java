package com.HauBaka.enums;

import com.HauBaka.utils.Utils;
import lombok.Getter;
import org.bukkit.Bukkit;

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
        NORMAL("§a"),
        INSANE("§c");

        @Getter
        private final String color;
        Type(String color) {
            this.color = color;
        }
        @Override
        public String toString() {
            return this.color + Utils.toBetterName(name());
        }
    }
    @Getter
    private final Mode mode;
    @Getter
    private final Type type;

    public ArenaVariant(Mode mode, Type type) {
        this.mode = mode;
        this.type = type;
    }
    public static ArenaVariant valueOf(String key) {
        String[] arr= key.split("_");
        if (arr.length != 2) {
            Bukkit.getLogger().warning("Unknow ArenaVariant: " + key);
            return null;
        }
        return new ArenaVariant(Mode.valueOf(arr[0]), Type.valueOf(arr[1]));
    }
}
