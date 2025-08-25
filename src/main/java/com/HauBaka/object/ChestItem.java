package com.HauBaka.object;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChestItem {
    private final ItemStack item;
    @Getter
    private final int chance;

    public ChestItem(ItemStack item, int chance) {
        this.item = item;
        this.chance = chance;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public static ItemStack buildItem(String material, byte data, int amount, String name,
                                      List<String> lore, Map<Enchantment, Integer> enchants,
                                      List<ItemFlag> flags) {
        Material mat = Material.matchMaterial(material);
        if (mat == null) throw new IllegalArgumentException("Invalid material: " + material);

        ItemStack item = new ItemStack(mat, amount, data);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        if (name != null)
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        if (lore != null) {
            List<String> coloredLore = lore.stream()
                    .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                    .collect(Collectors.toList());
            meta.setLore(coloredLore);
        }
        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            meta.addEnchant(entry.getKey(), entry.getValue(), true);
        }
        if (flags != null) meta.addItemFlags(flags.toArray(new ItemFlag[0]));
        item.setItemMeta(meta);
        return item;
    }
}
