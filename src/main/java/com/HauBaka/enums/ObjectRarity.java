package com.HauBaka.enums;

public enum ObjectRarity {
    COMMON("§a"),
    RARE("§b"),
    EPIC("§5"),
    LEGENDARY("§6"),
    SPECIAL("§c"),
    GOAT("§4§k");
    private final String prefix;
    ObjectRarity(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
