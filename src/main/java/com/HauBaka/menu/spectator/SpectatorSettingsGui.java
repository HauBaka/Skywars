package com.HauBaka.menu.spectator;

import com.HauBaka.menu.GuiItem;
import com.HauBaka.menu.GuiMenu;
import com.HauBaka.player.GamePlayer;
import com.HauBaka.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

public class SpectatorSettingsGui {
    public static void open(GamePlayer gamePlayer) {
        GuiMenu guiMenu = new GuiMenu("Spectator Settings", 36, gamePlayer);
        int[] items = {301, 305, 309, 317, 313};
        String[] names = {"§aNo Speed", "§aSpeed I", "§aSpeed II", "§aSpeed III", "§aSpeed IV"};
        for (int  i = 0; i < items.length; i++) {
            int finalI = i;
            guiMenu.setItem(11+i,new GuiItem(
                    Utils.buildItem(
                            new ItemStack(Material.getMaterial(items[i]), 1),
                            names[i],
                            null,
                            null
                    )
                ).setExecute(Arrays.asList(ClickType.values()), () -> {
                    Player player = gamePlayer.getPlayer();
                    player.removePotionEffect(PotionEffectType.SPEED);
                    if(finalI >0) player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1000000, finalI -1));
                    player.setFlySpeed((float) (0.1*(1+finalI/2.0)));
                    player.closeInventory();
                })
            );
        }
        guiMenu.open();
    }
}
