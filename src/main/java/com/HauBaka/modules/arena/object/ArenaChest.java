package com.HauBaka.modules.arena.object;

import com.HauBaka.modules.Utils;
import com.HauBaka.modules.arena.Arena;
import com.HauBaka.modules.arena.enums.ArenaState;
import com.HauBaka.modules.arena.enums.ArenaVariant;
import com.HauBaka.modules.file.FileConfig;
import com.HauBaka.modules.object.Hologram;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ArenaChest {
    // state(phase_1,2...) -> type(normal/insane) -> chestType(spawn/mid) -> list item
    private static Map<ArenaState, Map<ArenaVariant.Type, Map<String, List<ChestItem>>>> chestItems;

    private final Arena arena;
    @Getter
    private final Location location;
    @Getter
    private Hologram hologram;

    public ArenaChest(Arena arena, Location location) {
        this.arena = arena;
        this.location = location;
    }
    public Chest getChest() {
        if (location.getBlock().getType() != Material.CHEST) {
            return null;
        }
        return (Chest) location.getBlock().getState();
    }

    public List<Integer> getAvailableSlots() {
        List<Integer> slots = new ArrayList<>();
        Chest chest = getChest();
        if (chest == null) return  slots;
        ItemStack[] items = chest.getInventory().getContents();
        for (int i = 0;  i < items.length; ++i) {
            if (items[i] == null) slots.add(i);
        }
        return slots;
    }

    public void refill() {
        Chest bChest = getChest();
        if (bChest == null) return;

        List<Integer> availableSlots = getAvailableSlots();
        if (availableSlots.isEmpty()) return;

        List<ChestItem> itemList = new ArrayList<>(
                ArenaChest.getChestItems(arena.getState(), arena.getVariant().getType(), "spawns")
        );
        if (itemList.isEmpty()) return;

        List<ItemStack> itemsToAdd = pickRandomItems(itemList, Utils.randomInRange(2,Math.min(5, availableSlots.size())));
        for (ItemStack item : itemsToAdd) {
            int slot = availableSlots.get(Utils.randomInRange(0,availableSlots.size()-1));
            bChest.getInventory().setItem(slot, item);
            availableSlots.remove((Integer) slot);
        }
    }

    public static void init() {
        chestItems = new HashMap<>();
        FileConfig fileConfig = new FileConfig("refill.yml");

        for (String phaseKey : fileConfig.getConfig().getKeys(false)) {
            // Map string phase -> ArenaState
            ArenaState state;
            try {
                state = ArenaState.valueOf(phaseKey.toUpperCase()); // Ex: "phase_1" -> PHASE_1
            } catch (IllegalArgumentException e) {
                continue;
            }

            Map<ArenaVariant.Type, Map<String, List<ChestItem>>> phaseMap = new HashMap<>();

            for (String variantKey : fileConfig.getConfig().getConfigurationSection(phaseKey).getKeys(false)) {
                ArenaVariant.Type variant = ArenaVariant.Type.valueOf(variantKey.toUpperCase());
                Map<String, List<ChestItem>> typeMap = new HashMap<>();

                for (String chestType : fileConfig.getConfig().getConfigurationSection(phaseKey + "." + variantKey).getKeys(false)) {
                    List<Map<?, ?>> itemsList = fileConfig.getConfig().getMapList(phaseKey + "." + variantKey + "." + chestType);
                    List<ChestItem> chestItemList = new ArrayList<>();

                    for (Map<?, ?> itemMap : itemsList) {
                        double chance = ((Number) itemMap.get("chance")).doubleValue();
                        String material = (String) itemMap.get("item");
                        int amount = itemMap.containsKey("amount") ? ((Number) itemMap.get("amount")).intValue() : 1;
                        String name = (String) itemMap.getOrDefault("name", null);
                        List<String> lore = itemMap.containsKey("lore") ? (List<String>) itemMap.get("lore") : null;

                        Map<Enchantment, Integer> enchants = new HashMap<>();
                        if (itemMap.containsKey("enchants")) {
                            Map<String, Object> enchMap = (Map<String, Object>) itemMap.get("enchants");
                            for (Map.Entry<String, Object> e : enchMap.entrySet()) {
                                Enchantment ench = Enchantment.getByName(e.getKey());
                                int lvl = ((Number) e.getValue()).intValue();
                                if (ench != null) enchants.put(ench, lvl);
                            }
                        }

                        ItemStack itemStack = ChestItem.buildItem(material, amount, name, lore, enchants);
                        chestItemList.add(new ChestItem(itemStack, chance));
                    }

                    typeMap.put(chestType, chestItemList);
                }

                phaseMap.put(variant, typeMap);
            }

            chestItems.put(state, phaseMap);
        }
    }

    public static List<ChestItem> getChestItems(ArenaState state, ArenaVariant.Type variant, String chestType) {
        if (!chestItems.containsKey(state)) return Collections.emptyList();
        Map<ArenaVariant.Type, Map<String, List<ChestItem>>> phaseMap = chestItems.get(state);
        if (!phaseMap.containsKey(variant)) return Collections.emptyList();
        Map<String, List<ChestItem>> typeMap = phaseMap.get(variant);
        return typeMap.getOrDefault(chestType, Collections.emptyList());
    }

    private List<ItemStack> pickRandomItems(List<ChestItem> itemList, int amount) {
        List<ItemStack> result = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < amount; i++) {
            double totalChance = itemList.stream().mapToDouble(ChestItem::getChance).sum();
            Iterator<ChestItem> iterator = itemList.iterator();
            double r = random.nextDouble() * totalChance;
            double cumulative = 0;

            while (iterator.hasNext()) {
                ChestItem item = iterator.next();
                cumulative += item.getChance();
                if (cumulative >= r) {
                    result.add(item.getItem().clone());
                    iterator.remove();
                    break;
                }
            }

        }

        return result;
    }
}
