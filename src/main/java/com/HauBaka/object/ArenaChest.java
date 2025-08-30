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
import io.netty.util.internal.ThreadLocalRandom;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Blocks;
import org.bukkit.*;
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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ArenaChest implements Listener {
    //variant(solo/doubles ... normal/insane) -> state(phase_1,2...)  -> chestType(spawn/mid) -> list item
    private static Map<ArenaVariant, Map<ArenaState, Map<ArenaSetupStage, List<ChestItem>>>> chestItems;

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
        this.hologram = new Hologram(location.clone().add(0, 0.5, 0));
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

        List<ChestItem> itemList = new ArrayList<>(ArenaChest.getChestItems(arena.getVariant(), arena.getState(), type));
        if (itemList.isEmpty()) return;

        List<ItemStack> itemsToAdd = pickRandomItems(itemList, Utils.randomInRange(3,Math.min(6, availableSlots.size())));

        for (ItemStack item : itemsToAdd) {
            int slot = availableSlots.get(Utils.randomInRange(0,availableSlots.size()-1));
            bChest.getInventory().setItem(slot, item);
            availableSlots.remove((Integer) slot);
        }
        setOpened(false);
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

        for (String variantKey : fileConfig.getConfig().getKeys(false)) {

            ArenaVariant variant;
            try {
                variant = ArenaVariant.valueOf(variantKey);
            } catch (IllegalArgumentException e) {
                continue;
            }

            Map<ArenaState, Map<ArenaSetupStage, List<ChestItem>>> stageMap = new HashMap<>();
            for (String phaseKey : fileConfig.getConfig().getConfigurationSection(variantKey).getKeys(false)) {
                ArenaState stage;
                try {
                    stage = ArenaState.valueOf(phaseKey);
                } catch (IllegalArgumentException e) {
                    continue;
                }
                Map<ArenaSetupStage, List<ChestItem>> setupStageItems = new HashMap<>();

                for (String setupStage : fileConfig.getConfig().getConfigurationSection(variantKey + "." + phaseKey).getKeys(false)) {
                    ArenaSetupStage arenaSetupStage;

                    try {
                        arenaSetupStage = ArenaSetupStage.valueOf(setupStage);
                    } catch (IllegalArgumentException e) {
                        continue;
                    }

                    List<String> lines = fileConfig.getConfig().getStringList(variantKey + "." + phaseKey + "." + setupStage);
                    if (lines == null || lines.isEmpty()) continue;
                    List<ChestItem> items = new ArrayList<>();

                    for (String line : lines) {
                        ChestItem chestItem = parseChestItem(line);
                        if (chestItem != null) items.add(chestItem);
                    }
                    setupStageItems.put(arenaSetupStage, items);
                }

                stageMap.put(stage, setupStageItems);
            }

            chestItems.put(variant, stageMap);
        }
    }
    @SuppressWarnings("unchecked")
    private static ChestItem parseChestItem(String line) {
        try {
            if (line == null) return null;

            String[] splitByChance = line.split("\\|", 2);
            if (splitByChance.length < 2) throw new IllegalArgumentException("Invalid chest line (no '|'): " + line);

            String chancePart = splitByChance[0].trim();
            int chance = Integer.parseInt(chancePart);

            String right = splitByChance[1].trim();
            String[] tokens = right.split(";");
            if (tokens.length == 0) throw new IllegalArgumentException("No material token: " + line);

            String materialToken = tokens[0].trim();
            String[] materialData = materialToken.split(":", 2);
            String matName = materialData[0].trim().toUpperCase();
            Material material = Material.getMaterial(matName);
            if (material == null) {
                Bukkit.getLogger().warning("[Refill] Unknown material '" + matName + "' in line: " + line);
                return null;
            }
            short durability = 0;
            if (materialData.length > 1) {
                try {
                    durability = Short.parseShort(materialData[1].trim());
                } catch (NumberFormatException ignored) {}
            }

            int amount = 1;
            ItemStack item = new ItemStack(material, 1, durability);
            ItemMeta meta = item.getItemMeta();

            for (int i = 1; i < tokens.length; i++) {
                String token = tokens[i].trim();
                if (token.isEmpty()) continue;

                // key:value hoặc key=value (chỉ split 1 lần)
                int sepIndex = token.indexOf(':');
                String key;
                String value;
                if (sepIndex > 0) {
                    key = token.substring(0, sepIndex).trim().toLowerCase();
                    value = token.substring(sepIndex + 1).trim();
                } else {
                    key = token.trim().toLowerCase();
                    value = "";
                }

                switch (key) {
                    case "name":
                        if (meta != null && !value.isEmpty()) {
                            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', value));
                        }
                        break;

                    case "amount":
                        try {
                            amount = Integer.parseInt(value);
                        } catch (NumberFormatException ignored) {  }
                        break;

                    case "lore":
                        if (meta != null && !value.isEmpty()) {
                            String[] loreParts = value.split(",");
                            List<String> lore = new ArrayList<>();
                            for (String l : loreParts) lore.add(ChatColor.translateAlternateColorCodes('&', l.trim()));
                            meta.setLore(lore);
                        }
                        break;
                    case "enchant":
                        if (meta != null && !value.isEmpty()) {
                            String[] enchSpecs = value.split(",");
                            for (String spec : enchSpecs) {
                                String[] parts = spec.split(":", 2);
                                if (parts.length < 2) continue;
                                String enchName = parts[0].trim().toUpperCase();
                                String lvlStr = parts[1].trim();
                                try {
                                    int lvl = Integer.parseInt(lvlStr);
                                    Enchantment ench = Enchantment.getByName(enchName);
                                    if (ench == null) {
                                        try { ench = Enchantment.getByName(enchName.replace(' ', '_')); } catch (Exception ignore) {}
                                    }
                                    if (ench != null) meta.addEnchant(ench, lvl, true);
                                } catch (NumberFormatException ignored) { }
                            }
                        }
                        break;
                    case "flag":
                        if (meta != null && !value.isEmpty()) {
                            String[] flags = value.split(",");
                            for (String f : flags) {
                                try {
                                    meta.addItemFlags(ItemFlag.valueOf(f.trim().toUpperCase()));
                                } catch (IllegalArgumentException ignored) { }
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
            item.setAmount(amount);
            if (meta != null) item.setItemMeta(meta);

            return new ChestItem(item, chance);
        } catch (Exception e) {
            Bukkit.getLogger().warning("[Refill] Failed to parse item line: " + line + " (" + e.getMessage() + ")");
            return null;
        }
    }


    public static List<ChestItem> getChestItems(ArenaVariant variant, ArenaState state, ArenaSetupStage chestType) {
        if (!chestItems.containsKey(variant)) return Collections.emptyList();

        Map<ArenaState, Map<ArenaSetupStage, List<ChestItem>>> phaseMap = chestItems.get(variant);
        if (!phaseMap.containsKey(state)) return Collections.emptyList();

        Map<ArenaSetupStage, List<ChestItem>> typeMap = phaseMap.get(state);
        return typeMap.getOrDefault(chestType, Collections.emptyList());
    }
    public void setOpened(boolean value) {
        this.isOpened = value;
        if (!value) {
            arena.getOpenedChests().remove(this);
            if (!hologram.isEmpty()) {
                hologram.setLine(0, "");
                hologram.setLine(1, "");
            }
            if (getChest() != null)
                sendOpenPacket(false);
        }
        else {
            arena.getOpenedChests().add(this);
            if (hologram.isEmpty())  {
                hologram.setLines("", "");
            }
            hologram.setLine(0, "&a"+ Utils.secondsToTime(arena.getTime()));
            hologram.setLine(1, isEmpty() ? "&cEmpty!" : "");
        }
    }
    public void sendOpenPacket(boolean open) {
        ((CraftWorld) location.getWorld()).getHandle().playBlockAction(
                new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()),
                Blocks.CHEST,
                1, open ? 1 : 0
        );
    }
    private List<ItemStack> pickRandomItems(List<ChestItem> itemList, int amount) {
        List<ItemStack> result = new ArrayList<>();
        if (itemList == null || itemList.isEmpty() || amount <= 0) return result;

        List<ChestItem> pool = new ArrayList<>(itemList);
        int size = Math.min(pool.size(), amount);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        int attempts = 0;
        int maxAttempts = Math.max(500, size * 50);

        while (result.size() < size && attempts < maxAttempts && !pool.isEmpty()) {
            attempts++;
            int index = rnd.nextInt(pool.size());
            ChestItem ci = pool.get(index);
            if (ci == null) {
                pool.remove(index);
                continue;
            }

            if (Utils.chanceOf(ci.getChance())) {
                ItemStack it = ci.getItem();
                if (it != null) result.add(it.clone());
                pool.remove(index);
            } else {
                if (attempts % 50 == 0) {
                    ChestItem pick = pool.remove(rnd.nextInt(pool.size()));
                    if (pick != null && pick.getItem() != null) result.add(pick.getItem().clone());
                }
            }
        }

        while (result.size() < size && !pool.isEmpty()) {
            ChestItem pick = pool.remove(ThreadLocalRandom.current().nextInt(pool.size()));
            if (pick != null && pick.getItem() != null) result.add(pick.getItem().clone());
        }

        return result;
    }

    public void destroy() {
        setOpened(false);
        HandlerList.unregisterAll(this);
        if (!hologram.isEmpty()) hologram.destroy();
    }

    @EventHandler
    public void openChest(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null ||
                event.getClickedBlock().getType() != Material.CHEST) return;


        Chest chest = (Chest) event.getClickedBlock().getState();
        if (!chest.getLocation().equals(location)) return;
        if (!arena.getAlive_players().contains(GamePlayer.getGamePlayer(event.getPlayer()))) {
            event.setCancelled(true);
            return;
        }

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
        setOpened(true);
        Bukkit.getPluginManager().callEvent(new ChestCloseEvent(
                arena,
                GamePlayer.getGamePlayer((Player) event.getPlayer()),
                this
        ));
    }

    @EventHandler
    public void chestBreak(BlockBreakEvent event) {
        if (!event.getBlock().getLocation().equals(location)) return;
        Player player = event.getPlayer();
        if (player == null || !arena.getAlive_players().contains(GamePlayer.getGamePlayer(player))) {
            event.setCancelled(true);
            return;
        }
        if (!hologram.isEmpty()) hologram.destroy();
        setOpened(false);
        Bukkit.getPluginManager().callEvent(new ChestBreakEvent(
                arena,
                GamePlayer.getGamePlayer(event.getPlayer()),
                this
        ));
    }

}
