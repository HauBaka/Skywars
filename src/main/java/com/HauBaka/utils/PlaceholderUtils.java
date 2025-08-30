package com.HauBaka.utils;

import com.HauBaka.arena.Arena;
import com.HauBaka.enums.PlaceholderVariable;
import com.HauBaka.player.GamePlayer;

import java.util.Map;

public class PlaceholderUtils {
    public static String replacePlaceholders(String str, Arena arena) {
        if (arena == null || str == null || str.isEmpty()) {
            return "";
        }
        return str.replace(PlaceholderVariable.DATE.getPlaceholder(), Utils.getTodayFormat("dd/MM/yy"))
                .replace(PlaceholderVariable.GAME_ID.getPlaceholder(), arena.getId())
                .replace(PlaceholderVariable.GAME_PLAYERS.getPlaceholder(), String.valueOf(arena.getPlayers().size()))
                .replace(PlaceholderVariable.GAME_MAX_PLAYERS.getPlaceholder(), String.valueOf(arena.getVariant().getMode().getMaxPlayer()))
                .replace(PlaceholderVariable.STATE.getPlaceholder(), arena.getState().getName())
                .replace(PlaceholderVariable.MAP_COLORED_NAME.getPlaceholder(), arena.getName())
                .replace(PlaceholderVariable.MODE_COLORED_NAME.getPlaceholder(), arena.getVariant().getType().toString())
                .replace(PlaceholderVariable.NEXT_EVENT_NAME.getPlaceholder(), arena.getState().getNext() != null ? arena.getState().getNext().getName() : "")
                .replace(PlaceholderVariable.TIMER.getPlaceholder(), Utils.secondsToTime(arena.getTime()))
                .replace(PlaceholderVariable.PLAYERS_LEFT.getPlaceholder(), String.valueOf(arena.getAlive_players().size()))
                .replace(PlaceholderVariable.TEAMS_LEFT.getPlaceholder(), String.valueOf(arena.getAlive_teams().size()));
    }
    public static String replacePlaceholders(String str, Arena arena, GamePlayer gamePlayer) {
        return replacePlaceholders(str, arena)
                .replace(PlaceholderVariable.PLAYER.getPlaceholder(), gamePlayer.getPlayer().getDisplayName())
                .replace(PlaceholderVariable.KILLS.getPlaceholder(), String.valueOf(arena.getKills().get(gamePlayer)))
                .replace(PlaceholderVariable.ASSISTS.getPlaceholder(), String.valueOf(arena.getAssists().get(gamePlayer)));
    }
    public static String replacePlaceholders(String str, Map<String, String> map) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            str = str.replace(entry.getKey(), entry.getValue());
        }
        return str;
    }
}
