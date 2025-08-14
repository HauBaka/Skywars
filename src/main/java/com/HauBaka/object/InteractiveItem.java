package com.HauBaka.object;

import com.HauBaka.Skywars;
import com.HauBaka.utils.NBTUtil;
import com.HauBaka.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class InteractiveItem implements Listener {
    @Getter
    private final ItemStack item;
    private final Map<Action, Consumer<PlayerInteractEvent>> consumers;
    private final String key;
    @Setter @Getter
    private boolean allowChangeSlot;

    public InteractiveItem(ItemStack item) {
        this.item = item.clone();
        this.consumers = new HashMap<>();
        this.key = Utils.generateID(4);
        this.allowChangeSlot = false;
        NBTUtil.setString(this.item, this.key, "1");
        Bukkit.getPluginManager().registerEvents(this, Skywars.getInstance());
    }
    public InteractiveItem setInteract(List<Action> actions, Consumer<PlayerInteractEvent> consumer) {
        for (Action action : actions) setInteract(action, consumer);
        return this;
    }
    public InteractiveItem setInteract(Action action, Consumer<PlayerInteractEvent> consumer) {
        consumers.put(action, consumer);
        return this;
    }

    public InteractiveItem removeInteract(Action action) {
        consumers.remove(action);
        return this;
    }

    @EventHandler
    public void interact(PlayerInteractEvent event) {
        ItemStack hand = event.getItem();
        if (hand == null || !hand.isSimilar(this.item) || !NBTUtil.hasKey(hand, this.key)) return;

        Consumer<PlayerInteractEvent> consumer = consumers.get(event.getAction());
        if (consumer != null) consumer.accept(event);
    }

    @EventHandler
    public void invClick(InventoryClickEvent event) {
        ItemStack current = event.getCurrentItem();
        if (current == null || !current.isSimilar(this.item) || !NBTUtil.hasKey(current, this.key)) return;

        event.setCancelled(!allowChangeSlot);
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
    }
}
