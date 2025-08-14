package com.HauBaka.object;

import com.HauBaka.Skywars;
import com.HauBaka.utils.NBTUtil;
import com.HauBaka.utils.Utils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class InteractiveItem implements Listener {
    @Getter
    private final ItemStack item;
    private final Map<Action, Consumer<PlayerInteractEvent>> consumers;
    private final String key;
    public InteractiveItem(ItemStack item) {
        this.item = item;
        this.consumers = new HashMap<>();
        this.key = Utils.generateID(4);
        NBTUtil.setString(item, this.key, "1");
        Bukkit.getPluginManager().registerEvents(this, Skywars.getInstance());
    }

    public void setInteract(Action action, Consumer<PlayerInteractEvent> consumer) {
        consumers.put(action, consumer);
    }

    @EventHandler
    public void interact(PlayerInteractEvent event) {
        ItemStack hand = event.getItem();
        if (hand == null || !item.isSimilar(hand) || !NBTUtil.hasKey(hand, this.key)) return;

        event.setCancelled(true);
        Consumer<PlayerInteractEvent> consumer = consumers.get(event.getAction());
        if (consumer != null) consumer.accept(event);
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
    }
}
