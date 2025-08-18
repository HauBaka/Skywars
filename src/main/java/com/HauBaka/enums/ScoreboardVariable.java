package com.HauBaka.enums;

import lombok.Getter;

public enum ScoreboardVariable {
    DATE("%date%"),
    GAME_ID("%game_id%"),
    GAME_PLAYERS("%game_players%"),
    GAME_MAX_PLAYERS("%game_max_players%"),
    STATE("%state%"),
    MAP_COLORED_NAME("%map_colored_name%"),
    MODE_COLORED_NAME("%mode_colored_name%"),
    NEXT_EVENT_NAME("%next_event_name%"),
    TIMER("%timer%"),
    PLAYERS_LEFT("%players_left%"),
    TEAMS_LEFT("%teams_left%"),
    KILLS("%kills%"),
    ASSISTS("%assists%");

    @Getter
    private final String placeholder;

    ScoreboardVariable(String placeholder) {
        this.placeholder = placeholder;
    }
}
