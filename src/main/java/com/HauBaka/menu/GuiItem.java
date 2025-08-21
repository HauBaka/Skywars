package com.HauBaka.menu;

import com.mojang.authlib.GameProfile;
import lombok.Getter;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

import com.mojang.authlib.properties.Property;
import org.bukkit.Material;

import java.lang.reflect.Field;

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
    public GuiItem setExecute(List<ClickType> clickTypes, Runnable runnable) {
        for (ClickType clickType : clickTypes) consumers.put(clickType, runnable);
        return this;
    }
    public void execute(ClickType clickType) {
        if (consumers.containsKey(clickType)) consumers.get(clickType).run();
    }

    @Override
    public GuiItem clone() {
        return new GuiItem(item.clone());
    }

    public static ItemStack buildHead(String url) {
        ItemStack item = new ItemStack(Material.SKULL_ITEM,1,(short) 3);
        if (url.isEmpty()) return item;
        SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField = null;
        try {
            profileField = itemMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(itemMeta, profile);
        }
        catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        item.setItemMeta(itemMeta);
        return item;

    }


}
