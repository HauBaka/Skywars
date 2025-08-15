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
    private final Map<ClickType, Consumer<Object>> consumers;
    public GuiItem(ItemStack item) {
        this.item = item;
        consumers = new IdentityHashMap<>();
    }
    public GuiItem setExecute(ClickType clickType, Consumer<Object> consumer) {
        consumers.put(clickType, consumer);
        return this;
    }
    public void execute(ClickType clickType, Object object) {
        if (consumers.containsKey(clickType)) consumers.get(clickType).accept(object);
    }

    @Override
    public GuiItem clone() {
        return new GuiItem(item.clone());
    }

}
