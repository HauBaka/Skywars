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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ArenaChest implements Listener {
    // state(phase_1,2...) -> type(normal/insane) -> chestType(spawn/mid) -> list item
    private static Map<ArenaState, Map<ArenaVariant, Map<ArenaSetupStage, List<ChestItem>>>> chestItems;

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
                ArenaChest.getChestItems(arena.getState(), arena.getVariant(), type)
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
            // Phase -> ArenaState
            ArenaState state;
            try {
                state = ArenaState.valueOf(phaseKey.toUpperCase()); // solo_insane_phase1 -> SOLO_INSANE_PHASE1
            } catch (IllegalArgumentException e) {
                continue;
            }

            Map<ArenaVariant, Map<ArenaSetupStage, List<ChestItem>>> variantMap = new HashMap<>();

            String inheritKey = fileConfig.getConfig().getString(phaseKey + ".inherit", null);
            if (inheritKey != null && fileConfig.getConfig().contains(inheritKey)) {
                ArenaState parentState;
                try {
                    parentState = ArenaState.valueOf(inheritKey.toUpperCase());
                    if (chestItems.containsKey(parentState)) {
                        // clone sâu map của parent
                        Map<ArenaVariant, Map<ArenaSetupStage, List<ChestItem>>> parentMap = chestItems.get(parentState);
                        for (Map.Entry<ArenaVariant, Map<ArenaSetupStage, List<ChestItem>>> e : parentMap.entrySet()) {
                            Map<ArenaSetupStage, List<ChestItem>> copyType = new HashMap<>();
                            for (Map.Entry<ArenaSetupStage, List<ChestItem>> e2 : e.getValue().entrySet()) {
                                copyType.put(e2.getKey(), new ArrayList<>(e2.getValue()));
                            }
                            variantMap.put(e.getKey(), copyType);
                        }
                    }
                } catch (IllegalArgumentException ignored) {}
            }
            // Parse các variant (solo_insane, teams_normal...)
            for (String variantKey : fileConfig.getConfig().getConfigurationSection(phaseKey).getKeys(false)) {
                if (variantKey.equalsIgnoreCase("inherit")) continue;

                ArenaVariant variant;
                try {
                    variant = ArenaVariant.valueOf(variantKey.toUpperCase());
                } catch (IllegalArgumentException e) {
                    continue;
                }

                Map<ArenaSetupStage, List<ChestItem>> typeMap = variantMap.getOrDefault(variant, new HashMap<>());

                for (String chestType : fileConfig.getConfig().getConfigurationSection(phaseKey + "." + variantKey).getKeys(false)) {
                    ArenaSetupStage stage;
                    try {
                        stage = ArenaSetupStage.valueOf(chestType.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        continue;
                    }

                    List<String> lines = fileConfig.getConfig().getStringList(phaseKey + "." + variantKey + "." + chestType);
                    List<ChestItem> chestItemList = typeMap.getOrDefault(stage, new ArrayList<>());

                    for (String line : lines) {
                        ChestItem item = parseChestItem(line);
                        if (item != null) chestItemList.add(item);
                    }

                    typeMap.put(stage, chestItemList);
                }

                variantMap.put(variant, typeMap);
            }

            chestItems.put(state, variantMap);
        }
    }
        @SuppressWarnings("unchecked")
        private static ChestItem parseChestItem(String line) {
        try {
            // tách chance
            String[] parts = line.split("\\|", 2);
            double chance = Double.parseDouble(parts[0].trim());
            String def = parts[1].trim();

            // tách item và meta
            String[] baseAndMeta = def.split(";", 2);
            String base = baseAndMeta[0]; // "WOOL:14"
            String meta = baseAndMeta.length > 1 ? baseAndMeta[1] : "";

            String[] itemSplit = base.split(":");
            String material = itemSplit[0].trim();
            byte data = itemSplit.length > 1 ? (byte) Integer.parseInt(itemSplit[1].trim()) : 0;

            int amount = 1;
            String name = null;
            List<String> lore = null;
            Map<Enchantment, Integer> enchants = new HashMap<>();
            List<ItemFlag> flags = new ArrayList<>();

            if (!meta.isEmpty()) {
                String[] options = meta.split(",");
                for (String opt : options) {
                    opt = opt.trim();
                    if (opt.startsWith("amount=")) {
                        amount = Integer.parseInt(opt.substring(7));
                    } else if (opt.startsWith("name=")) {
                        name = opt.substring(5).replace("\"", "");
                    } else if (opt.startsWith("lore=")) {
                        String raw = opt.substring(5).trim();
                        raw = raw.replaceAll("[{}\"]", "");
                        lore = Arrays.asList(raw.split(","));
                    } else if (opt.startsWith("enchant=")) {
                        String raw = opt.substring(8).replaceAll("[{}]", "");
                        for (String e : raw.split(",")) {
                            String[] kv = e.split(":");
                            if (kv.length != 2) continue;
                            Enchantment ench = Enchantment.getByName(kv[0].trim().toUpperCase());
                            int lvl = Integer.parseInt(kv[1].trim());
                            if (ench != null) enchants.put(ench, lvl);
                        }
                    } else if (opt.startsWith("flag=")) {
                        String raw = opt.substring(5).replaceAll("[{}]", "");
                        for (String f : raw.split(",")) {
                            try {
                                flags.add(ItemFlag.valueOf(f.trim().toUpperCase()));
                            } catch (IllegalArgumentException ignored) {}
                        }
                    }
                }
            }

            ItemStack itemStack = ChestItem.buildItem(material, data, amount, name, lore, enchants, flags);
            return new ChestItem(itemStack, chance);
        } catch (Exception e) {
            Bukkit.getLogger().warning("[Refill] Failed to parse item line: " + line);
            return null;
        }
    }

    public static List<ChestItem> getChestItems(ArenaState state, ArenaVariant variant, ArenaSetupStage chestType) {
        if (!chestItems.containsKey(state)) return Collections.emptyList();
        Map<ArenaVariant, Map<ArenaSetupStage, List<ChestItem>>> phaseMap = chestItems.get(state);
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
