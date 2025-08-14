package com.HauBaka.menu;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class GuiItem {
    @Getter
    private final ItemStack item;
    private final Consumer<Object> consumer;
    public GuiItem(ItemStack item, Consumer<Object> consumer) {
        this.item = item;
        this.consumer = consumer;
    }
    public void execute(Object object) {
        consumer.accept(object);
    }

    @Override
    public GuiItem clone() {
        return new GuiItem(item.clone(), consumer);
    }

}
