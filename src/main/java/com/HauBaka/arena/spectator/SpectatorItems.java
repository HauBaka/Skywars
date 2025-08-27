package com.HauBaka.arena.spectator;

import com.HauBaka.arena.Arena;
import com.HauBaka.menu.spectator.SpectatorGui;
import com.HauBaka.menu.spectator.SpectatorPlayAgainGui;
import com.HauBaka.menu.spectator.SpectatorSettingsGui;
import com.HauBaka.object.InteractiveItem;
import com.HauBaka.player.GamePlayer;
import com.HauBaka.utils.Utils;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;

public class SpectatorItems {
    public static void teleportCompass(GamePlayer gamePlayer, Arena arena, int slot) {
        gamePlayer.getPlayer().getInventory().setItem(slot, helper(
                Utils.buildItem(
                        new ItemStack(Material.COMPASS),
                        "§a§lTeleporter§r§7 (Right Click)",
                        Collections.singletonList("§7Right-click to spectate players!"),
                        null
                ),
                () -> SpectatorGui.open(gamePlayer, arena)
                ).getItem().clone()
        );
    }
    public static void settings(GamePlayer gamePlayer, Arena arena, int slot) {
        gamePlayer.getPlayer().getInventory().setItem(slot, helper(
                Utils.buildItem(
                        new ItemStack(Material.REDSTONE_COMPARATOR),
                        "§b§lSpectator Settings§r§7 (Right Click)",
                        Collections.singletonList("§7Right-click to change your spectator settings!"),
                        null
                ),
                () -> SpectatorSettingsGui.open(gamePlayer)
                ).getItem().clone()
        );
    }
    public static void playAgain(GamePlayer gamePlayer, Arena arena, int slot) {
        gamePlayer.getPlayer().getInventory().setItem(slot, helper(
                Utils.buildItem(
                        new ItemStack(Material.PAPER),
                        "§b§lPlay Again§r§7 (Right Click)",
                        Collections.singletonList("§7Right-click to play another game!"),
                        null
                ),
                () -> SpectatorPlayAgainGui.open(gamePlayer, arena)
                ).getItem().clone()
        );
    }
    public static void returnToLobby(GamePlayer gamePlayer, Arena arena, int slot) {
        gamePlayer.getPlayer().getInventory().setItem(slot, helper(
                Utils.buildItem(
                        new ItemStack(Material.BED),
                        "§c§lReturn to Lobby§r§7 (Right Click)",
                        Collections.singletonList("§7Right-click to leave to the lobby!"),
                        null
                ),
                () -> arena.removePlayer(gamePlayer)
                ).getItem().clone()
        );
    }
    private static InteractiveItem helper(ItemStack itemStack, Runnable action) {
        InteractiveItem item = new InteractiveItem(itemStack);
        item.setInteract(
                Arrays.asList(
                        Action.LEFT_CLICK_AIR,
                        Action.LEFT_CLICK_BLOCK,
                        Action.RIGHT_CLICK_AIR,
                        Action.RIGHT_CLICK_BLOCK
                ),
                e -> action.run()
        );
        return item;
    }

}
