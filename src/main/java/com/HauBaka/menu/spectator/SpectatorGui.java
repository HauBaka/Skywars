package com.HauBaka.menu.spectator;

import com.HauBaka.Skywars;
import com.HauBaka.arena.Arena;
import com.HauBaka.menu.GuiItem;
import com.HauBaka.menu.GuiMenu;
import com.HauBaka.player.GamePlayer;
import com.HauBaka.utils.Utils;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Collections;

public class SpectatorGui {
    public static void open(GamePlayer gamePlayer, Arena arena) {
        int size = (int) (Math.ceil(arena.getAlive_players().size()/9.0) * 9);
        GuiMenu guiMenu = new GuiMenu("Teleporter",size, gamePlayer);
        int slot = 0;
        for (GamePlayer alivePlayer : arena.getAlive_players()) {
            createItem(gamePlayer, alivePlayer,arena,guiMenu, slot++);
        }
        guiMenu.open();
    }
    private static void createItem(GamePlayer gamePlayer,GamePlayer alivePlayer, Arena arena, GuiMenu guiMenu, int slot) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (guiMenu.isDestroyed())  cancel();
                if (!arena.getAlive_players().contains(alivePlayer)) {
                    guiMenu.setItem(slot, new GuiItem(
                            Utils.buildItem(
                                    Material.BARRIER,
                                    alivePlayer.getPlayer().getDisplayName(),
                                    Collections.singletonList("&cDead ><"),
                                    null
                            )
                        ).setExecute(Arrays.asList(ClickType.values()), () -> {
                                gamePlayer.sendMessage("&4&lERORR!&r&c This player already dead!");
                                gamePlayer.getPlayer().closeInventory();
                            }
                        )
                    );
                    cancel();
                }
                guiMenu.setItem(slot, new GuiItem(
                        Utils.buildItem(head.clone(),
                                alivePlayer.getPlayer().getDisplayName(),
                                Arrays.asList(
                                        "§7Health:§f " + alivePlayer.getPlayer().getHealth()/20.0*100 +"%",
                                        "§7Food:§f 100"
                                ),
                                null
                        )
                    ).setExecute(Arrays.asList(ClickType.values()),() -> {
                        if (arena.getAlive_players().contains(alivePlayer)) gamePlayer.getPlayer().teleport(
                                alivePlayer.getPlayer().getLocation().add(0,1,0)
                        );
                        else gamePlayer.sendMessage("&4&lERORR!&r&c This player already dead!");
                        gamePlayer.getPlayer().closeInventory();
                    })
                );
            }
        }.runTaskTimerAsynchronously(Skywars.getInstance(),0L,20L);
    }
    private static ItemStack head = GuiItem.buildHead("https://textures.minecraft.net/texture/ca9c8753780ebc39c351da8efd91bce90bd8cca7b511f93e78df75f6615c79a6");

}
