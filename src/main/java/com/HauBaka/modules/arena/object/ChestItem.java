package com.HauBaka.modules.arena.object;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChestItem {
    private final ItemStack item;
    @Getter
    private final double chance;

    public ChestItem(ItemStack item, double chance) {
        this.item = item;
        this.chance = chance;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public static ItemStack buildItem(String material, int amount, String name,
                                      List<String> lore, Map<Enchantment, Integer> enchants) {
        ItemStack item = new ItemStack(Material.valueOf(material), amount);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        if (name != null) meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        if (lore != null) {
            List<String> coloredLore = lore.stream()
                    .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                    .collect(Collectors.toList());
            meta.setLore(coloredLore);
        }
        if (enchants != null) {
            enchants.forEach((ench, lvl) -> meta.addEnchant(ench, lvl, true));
        }
        item.setItemMeta(meta);
        return item;
    }
}
