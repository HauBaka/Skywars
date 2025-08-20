package com.HauBaka.object;

import com.HauBaka.Skywars;
import com.HauBaka.enums.ArenaSetupStage;
import com.HauBaka.event.ChestBreakEvent;
import com.HauBaka.event.ChestCloseEvent;
import com.HauBaka.event.ChestOpenEvent;
import com.HauBaka.player.GamePlayer;
import com.HauBaka.utils.Utils;
import com.HauBaka.arena.Arena;
import com.HauBaka.enums.ArenaState;
import com.HauBaka.enums.ArenaVariant;
import com.HauBaka.file.FileConfig;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Blocks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ArenaChest implements Listener {
    // state(phase_1,2...) -> type(normal/insane) -> chestType(spawn/mid) -> list item
    private static Map<ArenaState, Map<ArenaVariant.Type, Map<ArenaSetupStage, List<ChestItem>>>> chestItems;

    private final Arena arena;
    @Getter
    private final Location location;
    @Getter
    private final Hologram hologram;
    @Getter
    private final ArenaSetupStage type;
    @Getter
    private boolean isOpened;
    public ArenaChest(Arena arena, Location location, ArenaSetupStage type) {
        this.arena = arena;
        this.location = location;
        this.type = type;
        this.isOpened = false;
        this.hologram = new Hologram(location);
        hologram.setLines("", "");
        Bukkit.getPluginManager().registerEvents(this, Skywars.getInstance());
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
                ArenaChest.getChestItems(arena.getState(), arena.getVariant().getType(), type)
        );
        if (itemList.isEmpty()) return;

        List<ItemStack> itemsToAdd = pickRandomItems(itemList, Utils.randomInRange(2,Math.min(5, availableSlots.size())));
        for (ItemStack item : itemsToAdd) {
            int slot = availableSlots.get(Utils.randomInRange(0,availableSlots.size()-1));
            bChest.getInventory().setItem(slot, item);
            availableSlots.remove((Integer) slot);
        }
    }
    public boolean isEmpty() {
        Chest chest = getChest();
        if (chest == null) return true;
        for (ItemStack stack : chest.getInventory().getContents()) {
            if (stack != null) return false;
        }
        return true;
    }
    public static void init() {
        chestItems = new HashMap<>();
        FileConfig fileConfig = new FileConfig("refill.yml");
        fileConfig.saveDefaultConfig();
        for (String phaseKey : fileConfig.getConfig().getKeys(false)) {
            // Map string phase -> ArenaState
            ArenaState state;
            try {
                state = ArenaState.valueOf(phaseKey.toUpperCase()); // Ex: "phase_1" -> PHASE_1
            } catch (IllegalArgumentException e) {
                continue;
            }

            Map<ArenaVariant.Type, Map<ArenaSetupStage, List<ChestItem>>> phaseMap = new HashMap<>();

            for (String variantKey : fileConfig.getConfig().getConfigurationSection(phaseKey).getKeys(false)) {
                ArenaVariant.Type variant = ArenaVariant.Type.valueOf(variantKey.toUpperCase());
                Map<ArenaSetupStage, List<ChestItem>> typeMap = new HashMap<>();

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

                    typeMap.put(ArenaSetupStage.valueOf(chestType.toUpperCase()), chestItemList);
                }

                phaseMap.put(variant, typeMap);
            }

            chestItems.put(state, phaseMap);
        }
    }

    public static List<ChestItem> getChestItems(ArenaState state, ArenaVariant.Type variant, ArenaSetupStage chestType) {
        if (!chestItems.containsKey(state)) return Collections.emptyList();
        Map<ArenaVariant.Type, Map<ArenaSetupStage, List<ChestItem>>> phaseMap = chestItems.get(state);
        if (!phaseMap.containsKey(variant)) return Collections.emptyList();
        Map<ArenaSetupStage, List<ChestItem>> typeMap = phaseMap.get(variant);
        return typeMap.getOrDefault(chestType, Collections.emptyList());
    }
    public void setOpened(boolean value) {
        this.isOpened = value;
        if (!value) {
            hologram.clearLines();
            ((CraftWorld) location.getWorld()).getHandle().playBlockAction(
                    new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()),
                    Blocks.CHEST,
                    1,
                    0
            );
        }
        else {
            hologram.setLine(0, Utils.secondsToTime(arena.getTime()));
            ((CraftWorld) location.getWorld()).getHandle().playBlockAction(
                    new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()),
                    Blocks.CHEST,
                    1,
                    1
            );
        }
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

    public void destroy() {
        HandlerList.unregisterAll(this);
        hologram.destroy();
    }

    @EventHandler
    public void openChest(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.CHEST) return;

        Chest chest = (Chest) event.getClickedBlock().getState();
        if (!chest.getLocation().equals(location)) return;
        setOpened(true);
        Bukkit.getPluginManager().callEvent(new ChestOpenEvent(
                arena,
                GamePlayer.getGamePlayer((Player) event.getPlayer()),
                this
        ));

    }

    @EventHandler
    public void closeChest(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof Chest)) return;
        if (event.getPlayer() == null) return;
        Chest chest = (Chest) event.getInventory().getHolder();
        if (!chest.getLocation().equals(location)) return;
        Bukkit.getPluginManager().callEvent(new ChestCloseEvent(
                arena,
                GamePlayer.getGamePlayer((Player) event.getPlayer()),
                this
        ));
    }

    @EventHandler
    public void chestBreak(BlockBreakEvent event) {
        if (!event.getBlock().getLocation().equals(location)) return;
        Chest chest = getChest();
        if (chest == null) return;
        setOpened(false);
        Bukkit.getPluginManager().callEvent(new ChestBreakEvent(
                arena,
                GamePlayer.getGamePlayer(event.getPlayer()),
                this
        ));
    }

}
