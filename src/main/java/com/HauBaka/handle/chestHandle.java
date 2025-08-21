package com.HauBaka.handle;

import com.HauBaka.event.ChestCloseEvent;
import com.HauBaka.event.ChestOpenEvent;
import com.HauBaka.object.ArenaChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class chestHandle implements Listener {
    @EventHandler
    public void open(ChestOpenEvent event) {
        ArenaChest chest = event.getArenaChest();
        chest.setOpened(chest.getChest() != null);
        System.out.println(chest.getLocation().toString());
    }

    @EventHandler
    public void close(ChestCloseEvent event) {
        ArenaChest chest = event.getArenaChest();
        if (chest.getChest() != null) {
            chest.getHologram().setLine(1,
                    ( chest.isEmpty() && chest.isOpened()) ? "&cEmpty!" : "");
        }
    }
}
