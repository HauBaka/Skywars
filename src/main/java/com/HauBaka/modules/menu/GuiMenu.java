package com.HauBaka.modules.menu;

import com.HauBaka.Skywars;
import com.HauBaka.modules.player.GamePlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

public class GuiMenu implements Listener {
    private static final Map<GamePlayer, Stack<GuiMenu>> guiMenus = new ConcurrentHashMap<>();

    private final GamePlayer gamePlayer;
    private final Map<Integer, GuiItem> itemSlots;
    @Getter
    private final Inventory inventory;

    public GuiMenu(String name, int size, GamePlayer owner) {
        this.inventory = Bukkit.createInventory(null, size, name);
        this.itemSlots = new ConcurrentHashMap<>();
        this.gamePlayer = owner;
    }

    public void open() {
        if (!guiMenus.containsKey(gamePlayer)) guiMenus.put(gamePlayer, new Stack<>());
        guiMenus.get(gamePlayer).add(this);
        gamePlayer.getPlayer().openInventory(inventory);
        Bukkit.getPluginManager().registerEvents(this, Skywars.getInstance());
    }

    public void addItem(int slot, GuiItem item) {
        this.itemSlots.put(slot, item);
        this.inventory.setItem(slot, item.getItem());
        gamePlayer.getPlayer().updateInventory();
    }

    private void execute(int slot) {
        GuiItem item = itemSlots.get(slot);
        if (item != null) {
            item.execute(gamePlayer);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getWhoClicked().equals(gamePlayer.getPlayer())) return;

        event.setCancelled(true);
        execute(event.getSlot());
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        if (!event.getPlayer().equals(gamePlayer.getPlayer())) return;
        if (!event.getInventory().equals(inventory)) return;

        Bukkit.getScheduler().runTaskLater(Skywars.getInstance(), () -> {
            Stack<GuiMenu> stack = guiMenus.get(gamePlayer);
            if (stack == null || stack.empty() || gamePlayer.getPlayer().isOnline() ||
                    !gamePlayer.getPlayer().getOpenInventory().getTopInventory().equals(stack.peek().getInventory()))
                closeAllMenu(gamePlayer);
        }, 1L);
    }

    private static void closeAllMenu(GamePlayer gamePlayer) {
        Stack<GuiMenu> menus = guiMenus.get(gamePlayer);
        while (!menus.empty()) {
            GuiMenu menu = menus.pop();
            menu.destroy();
        }
        guiMenus.remove(gamePlayer);
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
    }
}
