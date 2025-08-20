package com.HauBaka.menu;

import lombok.Getter;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class GuiItem {
    @Getter
    private final ItemStack item;
    private final Map<ClickType, Runnable> consumers;
    public GuiItem(ItemStack item) {
        this.item = item;
        consumers = new IdentityHashMap<>();
    }
    public GuiItem setExecute(ClickType clickType, Runnable runnable) {
        consumers.put(clickType, runnable);
        return this;
    }
    public void execute(ClickType clickType) {
        if (consumers.containsKey(clickType)) consumers.get(clickType).run();
    }

    @Override
    public GuiItem clone() {
        return new GuiItem(item.clone());
    }

}
