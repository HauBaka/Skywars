package com.HauBaka.enums;

import com.HauBaka.utils.Utils;
import lombok.Getter;

public enum Direction {
    SOUTH(0f),
    WEST(90f),
    NORTH(180f),
    EAST(270f);

    @Getter
    private final float yaw;

    Direction(float yaw) {
        this.yaw = yaw;
    }
    @Override
    public String toString() {
        String name = name().toLowerCase();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
    public static Direction fromYaw(float yaw) {
        yaw = Utils.getAbsoluteYaw(yaw);
        for (Direction dir : values()) {
            if (dir.yaw == yaw) return dir;
        }
        return SOUTH;
    }
}
