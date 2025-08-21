package com.HauBaka.arena.setup;

import com.HauBaka.enums.ArenaSetupStage;
import com.HauBaka.file.FileConfig;
import com.HauBaka.object.Hologram;
import com.HauBaka.object.InteractiveItem;
import com.HauBaka.object.TemplateBlock;
import com.HauBaka.player.GamePlayer;
import com.HauBaka.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ArenaSetupItems {
    public static final InteractiveItem teleportCompass = new InteractiveItem(
            Utils.buildItem(Material.COMPASS, "&b&lTELEPORT COMPASS",
                    Arrays.asList(
                            "&7Left click to target block",
                            "&7to &ateleport&7 to it's location"
                    ),
                    null)
    ).setInteract(Arrays.asList(Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK), (event) -> {
        event.setCancelled(true);
        Player player = event.getPlayer();
        Location loc = Utils.getTargetBlockLocation(player);
        if (loc == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4&lERROR!&r&c Too far!"));
            player.playSound(player.getLocation(), Sound.ENDERMAN_HIT, 1f, 1f);
            return;
        }
        player.teleport(loc);
        player.playSound(loc, Sound.ENDERMAN_TELEPORT, 1f, 1f);
    });
    public static final InteractiveItem lobbyPortal = new InteractiveItem(
            Utils.buildItem(Material.ENDER_PORTAL_FRAME, "&a&lADD LOBBY",
                    Collections.singletonList("&7Place to set lobby location"),
                    null
            )
    ).setInteract(Action.RIGHT_CLICK_BLOCK, event -> {
        ArenaSetup arenaSetup = ArenaSetupManager.getByEditor(GamePlayer.getGamePlayer(event.getPlayer()));
        if (arenaSetup == null) return;

        event.setCancelled(true);
        Location loc = event.getClickedBlock().getLocation().add(0,1,0);

        arenaSetup.setLobby(loc);

    });
    public static final InteractiveItem spawnBeacon = new InteractiveItem(
            Utils.buildItem(Material.BEACON, "&a&lADD SPAWN",
                    Collections.singletonList("&7Place to add spawn location"),
                    null
            )
    ).setInteract(Action.RIGHT_CLICK_BLOCK, event -> {
        ArenaSetup arenaSetup = ArenaSetupManager.getByEditor(GamePlayer.getGamePlayer(event.getPlayer()));
        if (arenaSetup == null) return;

        event.setCancelled(true);
        Location loc = event.getClickedBlock().getLocation().add(0,1,0);
        loc.setYaw(Utils.getAbsoluteYaw(event.getPlayer().getLocation().getYaw()));
        arenaSetup.addTeam(loc);
    });
    public static final InteractiveItem chestStick = new InteractiveItem(
            Utils.buildItem(
                    Material.STICK,
                    "&e&lADD CHEST",
                    Arrays.asList(
                            "&eLeft click&7 to chest to",
                            "&7set it as spawn's chests",
                            "",
                            "&eRight click&7 to add nearby",
                            "&7chests."
                    ),
                    null
            ))
            .setInteract(Action.LEFT_CLICK_BLOCK, event -> {
                event.setCancelled(true);

                ArenaSetup arenaSetup = ArenaSetupManager.getByEditor(GamePlayer.getGamePlayer(event.getPlayer()));
                if (arenaSetup == null) return;

                if (arenaSetup.getCurrentSpawn() == 0 || arenaSetup.getCurrentSpawn() > ArenaSetup.MAX_SPAWNS) {
                    arenaSetup.getEditor().sendMessage("&4&lERROR!&r&c No spawn provided!");
                    return;
                }

                if (arenaSetup.getSpawnChests().get(arenaSetup.getCurrentSpawn()).size() == ArenaSetup.CHEST_PER_SPAWN) {
                    arenaSetup.getEditor().sendMessage("&4&lERROR!&r&c Reached maximum chests per spawn!");
                    return;
                }

                Block block = event.getClickedBlock();
                if (block == null || !block.getType().equals(Material.CHEST)) return;

                arenaSetup.addChest(block,ArenaSetupStage.SPAWN);
            })
            .setInteract(Arrays.asList(Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR), event -> {
                event.setCancelled(true);
                GamePlayer editor = GamePlayer.getGamePlayer(event.getPlayer());
                ArenaSetup arenaSetup = ArenaSetupManager.getByEditor(editor);
                if (arenaSetup == null) return;

                if (arenaSetup.getCurrentSpawn() == 0 || arenaSetup.getCurrentSpawn() > ArenaSetup.MAX_SPAWNS) {
                    arenaSetup.getEditor().sendMessage("&4&lERROR!&r&c No spawn provided!");
                    return;
                }
                if (arenaSetup.getSpawnChests().get(arenaSetup.getCurrentSpawn()).size() == ArenaSetup.CHEST_PER_SPAWN) {
                    arenaSetup.getEditor().sendMessage("&4&lERROR!&r&c Reached maximum chests per spawn!");
                    return;
                }
                addNearbyChests(editor, arenaSetup, event.getPlayer().getLocation(), 20);
            });
    public static final InteractiveItem midChestStick = new InteractiveItem(
            Utils.buildItem(Material.BLAZE_ROD, "&6&lMID CHEST ADDER",
                    Arrays.asList(
                            "&7Left click to chest at mid",
                            "&7to add it to mid chest list.",
                            "",
                            "&7Break to remove it."
                    ),
                    null)
    ).setInteract(Action.LEFT_CLICK_BLOCK, event -> {
        event.setCancelled(true);

        ArenaSetup arenaSetup = ArenaSetupManager.getByEditor(GamePlayer.getGamePlayer(event.getPlayer()));
        if (arenaSetup == null) return;
        Block block = event.getClickedBlock();
        if (block == null || !block.getType().equals(Material.CHEST)) return;
        arenaSetup.addChest(block, ArenaSetupStage.MID);
    });
    public static final InteractiveItem cancelItem = new InteractiveItem(
            Utils.buildItem(
                    new ItemStack(Material.WOOL, 1, (byte) 14),
                    "&c&lCANCEL",
                    Collections.emptyList(),
                    null
            ))
            .setInteract(Arrays.asList(Action.values()), event -> {
                event.setCancelled(true);

                ArenaSetup arenaSetup = ArenaSetupManager.getByEditor(GamePlayer.getGamePlayer(event.getPlayer()));
                if (arenaSetup == null) return;

                arenaSetup.stopEdit();
            });
    public static final InteractiveItem nextStageItem = new InteractiveItem(
            Utils.buildItem(
                    new ItemStack(Material.WOOL, 1, (byte) 5),
                    "&2&lNEXT STAGE",
                    Collections.emptyList(),
                    null
            ))
            .setInteract(
                    Arrays.asList(Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR), event -> {
                        event.setCancelled(true);

                        ArenaSetup arenaSetup = ArenaSetupManager.getByEditor(GamePlayer.getGamePlayer(event.getPlayer()));
                        if (arenaSetup == null) return;
                        if (!arenaSetup.isValid()) return;

                        arenaSetup.setStage(ArenaSetupStage.MID);
                    });
    public static final InteractiveItem previousStageItem = new InteractiveItem(
            Utils.buildItem(
                    new ItemStack(Material.WOOL, 1, (byte) 1),
                    "&2&lPREVIOUS STAGE",
                    Collections.emptyList(),
                    null
            ))
            .setInteract(
                    Arrays.asList(Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR),
                    event -> {
                        event.setCancelled(true);
                        ArenaSetup arenaSetup = ArenaSetupManager.getByEditor(GamePlayer.getGamePlayer(event.getPlayer()));
                        if (arenaSetup == null) return;
                        arenaSetup.setStage(ArenaSetupStage.SPAWN);
                    });
    public static final InteractiveItem saveItem = new InteractiveItem(
            Utils.buildItem(
                    new ItemStack(Material.WOOL, 1, (byte) 5),
                    "&a&lSAVE",
                    Collections.emptyList(),
                    null
            ))
            .setInteract(
                    Arrays.asList(Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR),
                    event -> {
                        event.setCancelled(true);

                        ArenaSetup arenaSetup = ArenaSetupManager.getByEditor(GamePlayer.getGamePlayer(event.getPlayer()));
                        if (arenaSetup == null) return;

                        arenaSetup.save();
                    });

    public static void addNearbyChests(GamePlayer gamePlayer, ArenaSetup arenaSetup, Location loc, int radius) {
        gamePlayer.sendMessage("executed");
        World world = loc.getWorld();
        int baseX = loc.getBlockX();
        int baseY = loc.getBlockY();
        int baseZ = loc.getBlockZ();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = world.getBlockAt(baseX + x, baseY + y, baseZ + z);
                    if (block == null || !block.getType().equals(Material.CHEST)) continue;
                    arenaSetup.addChest(block, ArenaSetupStage.SPAWN);
                    gamePlayer.getPlayer().playSound(loc,Sound.ORB_PICKUP,1f,1f);
                }
            }
        }
    }

}
