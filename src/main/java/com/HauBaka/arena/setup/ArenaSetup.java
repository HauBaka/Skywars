package com.HauBaka.arena.setup;

import com.HauBaka.Skywars;
import com.HauBaka.arena.TemplateArena;
import com.HauBaka.enums.ArenaSetupStage;
import com.HauBaka.file.FileConfig;
import com.HauBaka.menu.GuiItem;
import com.HauBaka.menu.GuiMenu;
import com.HauBaka.object.Hologram;
import com.HauBaka.object.InteractiveItem;
import com.HauBaka.player.GamePlayer;
import com.HauBaka.utils.Utils;
import com.HauBaka.world.WorldManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;
import java.util.function.Consumer;

public class ArenaSetup {
    private enum Direction {
        SOUTH(0f),
        WEST(90f),
        NORTH(180f),
        EAST(270f);

        @Getter
        private final float yaw;

        Direction(float yaw) {
            this.yaw = yaw;
        }
        @Override
        public String toString() {
            String name = name().toLowerCase();
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        private static Direction fromYaw(float yaw) {
            yaw = Utils.getAbsoluteYaw(yaw);
            for (Direction dir : values()) {
                if (dir.yaw == yaw) return dir;
            }
            return SOUTH;
        }
    }

    private static class TemplateBlock implements Listener {
        private static final Map<World, List<TemplateBlock>> listBlocks = new HashMap<>();
        @Getter
        private final int x;
        @Getter
        private final int y;
        @Getter
        private final int z;
        @Getter
        private final Hologram hologram;
        Direction direction;
        @Getter
        private int teamNumber;
        @Setter
        private Consumer<BlockBreakEvent> consumer;
        private final Location loc;
        TemplateBlock(Location loc, int teamNumber) {
            this.x = loc.getBlockX();
            this.y = loc.getBlockY();
            this.z = loc.getBlockZ();
            this.teamNumber = teamNumber;
            this.loc = loc;
            direction = Direction.fromYaw(loc.getYaw());

            hologram = new Hologram(loc);
            hologram.addLine("&aTeam " + teamNumber +"'s spawn");
            hologram.addLine("&eDirection: &a" + direction.toString());
            hologram.addLine("");
            hologram.addLine("&e&lRIGHT CLICK TO CHANGE!");

            if (!listBlocks.containsKey(loc.getWorld())) listBlocks.put(loc.getWorld(), new ArrayList<>());
            listBlocks.get(loc.getWorld()).add(this);

            Bukkit.getPluginManager().registerEvents(this, Skywars.getInstance());
        }
        private void addYaw() {
            Direction newDirection = Direction.fromYaw(this.direction.getYaw() + (float) 90.0);
            setDirection(newDirection);
        }
        private void setDirection(Direction direction) {
            this.direction = direction;
            hologram.setLine(1, "&eDirection: &a" + direction.toString());
        }
        private void setTeam(int teamNumber) {
            if (teamNumber == this.teamNumber) return;
            this.teamNumber = teamNumber;
            hologram.setLine(0, "&aTeam " + teamNumber +"'s spawn");
        }
        private TemplateArena.TemplateLocation toTemplateLocation() {
            return new TemplateArena.TemplateLocation(x,y,z,direction.getYaw(),0f);
        }
        @EventHandler
        public void breakEvent(BlockBreakEvent event) {
            if (event.getBlock().getLocation().equals(this.loc)) {
                event.setCancelled(true);
                if (consumer != null) consumer.accept(event);
            }
        }
        private void destroy() {
            hologram.destroy();
            HandlerList.unregisterAll(this);
        }
        private static void removeWorld(World world) {
            if (!listBlocks.containsKey(world)) return;
            for (TemplateBlock templateBlock : listBlocks.get(world)) templateBlock.destroy();
            listBlocks.remove(world);
        }
    }

    private static final int MAX_SPAWNS = 12;
    private static final int CHEST_PER_SPAWN = 3;
    @Getter
    private final GamePlayer editor;
    @Getter
    private final String mapName;
    @Getter
    private World world;
    @Getter
    private ArenaSetupStage stage;
    @Getter
    private final Map<Integer, TemplateArena.TemplateLocation> spawns;
    @Getter
    private final Map<Integer, List<TemplateArena.TemplateLocation>> spawnChests;
    @Getter
    private final List<TemplateArena.TemplateLocation> midChests;
    @Getter
    private Map<Integer, ItemStack> oldInventory;
    @Getter @Setter
    private int currentSpawn;
    public ArenaSetup(String mapName, GamePlayer editor) {
        this.mapName = mapName.toLowerCase();
        this.editor = editor;
        this.spawns = new HashMap<>();
        this.spawnChests = new HashMap<>();
        this.midChests = new ArrayList<>();
        this.currentSpawn = 0;
        WorldManager.cloneWorld(this.mapName, w -> {
            this.world = w;
            joinEdit();
        });
    }
    public void joinEdit() {
        editor.getPlayer().teleport(this.world.getSpawnLocation());
        setStage(ArenaSetupStage.SPAWN);
    }
    public void setStage(ArenaSetupStage stage) {
        this.stage = stage;
        setItems();
    }

    public void stopEdit() {
        this.editor.getPlayer().getInventory().clear();
        for (int slot : oldInventory.keySet())
            this.editor.getPlayer().getInventory().setItem(slot, oldInventory.get(slot));

        TemplateBlock.removeWorld(world);
        ArenaSetupManager.removeEdit(this);
    }

    private void setItems() {
        Player player = this.editor.getPlayer();
        if (player == null || !player.isOnline()) return;

        player.closeInventory();
        PlayerInventory inventory = player.getInventory();
        oldInventory = new HashMap<>();

        for (int i = 0 ; i < inventory.getSize(); ++i) {
            ItemStack item = inventory.getItem(i);
            if (item != null)
                this.oldInventory.put(i, inventory.getItem(i));
        }

        inventory.clear();
        inventory.setItem(0, teleportCompass.getItem());

        inventory.setItem(8, cancelItem.getItem());
        switch (stage) {
            case SPAWN:
                inventory.setItem(2, spawnBeacon.getItem());
                inventory.setItem(3,chestStick.getItem());
                inventory.setItem(5, nextStageItem.getItem());
                break;
            case MID:
                inventory.setItem(3, midChestStick.getItem());
                inventory.setItem(5, previousStageItem.getItem());
                inventory.setItem(7, saveItem.getItem());
                break;
        }
    }

    static final int[] invLocations = {10,11,12,13,14,15,16,19,20,21,22,23};
    public void openSpawnsMenu(TemplateBlock templateBlock) {
        GuiMenu guiMenu = new GuiMenu(Utils.toBetterName(mapName) +"'s spawns", 36, editor);
        for (int i = 0; i < MAX_SPAWNS; ++i) {
            int team = i+1;
            if (team != templateBlock.getTeamNumber()) {
                guiMenu.setItem(invLocations[i], new GuiItem(
                        Utils.buildItem(
                                new ItemStack(Material.WOOL, team, (byte) (spawns.get(team) == null ? 7 : 5)),
                                "&aTeam " + team,
                                Arrays.asList(
                                        "&eLeft click&7 to set current",
                                        "&7location to &ateam " + team + "'s",
                                        "&aspawn&7.",
                                        "",
                                        "&eRight click&7 to &arotate yaw."),
                                null)
                        ).setExecute(ClickType.LEFT,o -> {
                            spawns.put(team, templateBlock.toTemplateLocation());
                            spawnChests.put(team, spawnChests.getOrDefault(templateBlock.getTeamNumber(),new ArrayList<>()));

                            spawns.remove(templateBlock.getTeamNumber());
                            spawnChests.remove(templateBlock.getTeamNumber());

                            templateBlock.setTeam(team);

                            guiMenu.setItem(
                                    invLocations[team-1],
                                    new GuiItem(
                                            Utils.buildItem(
                                                    new ItemStack(Material.WOOL, team, (byte) 14),
                                                    "&aTeam" + team,
                                                    Collections.singletonList("&eThis is current location!"),
                                                    null
                                            )
                                    ).setExecute(ClickType.LEFT, o1 -> {
                                        editor.getPlayer().sendMessage("&4&lERROR &cTeam " + team +"'s spawn is current location!");
                                        editor.getPlayer().playSound(editor.getPlayer().getLocation()
                                                , Sound.ENDERMAN_HIT, 1f, 1f);
                                    }).setExecute(ClickType.RIGHT, o1 -> {
                                        templateBlock.addYaw();
                                        editor.getPlayer().playSound(editor.getPlayer().getLocation(), Sound.ORB_PICKUP, 1f, 1f);
                                    })

                            );
                        }).setExecute(ClickType.RIGHT, o1 -> {
                            templateBlock.addYaw();
                            editor.getPlayer().playSound(editor.getPlayer().getLocation(), Sound.ORB_PICKUP, 1f, 1f);
                        })

                );
            } else {
                guiMenu.setItem(
                        invLocations[team-1],
                        new GuiItem(
                            Utils.buildItem(
                                    new ItemStack(Material.WOOL, team, (byte) 14),
                                    "&aTeam" + team,
                                    Collections.singletonList("&eThis is current location!"),
                                    null
                            )
                        ).setExecute(ClickType.LEFT, o-> {
                            editor.getPlayer().sendMessage("&4&lERROR &cTeam " + team +"'s spawn is current location!");
                            editor.getPlayer().playSound(editor.getPlayer().getLocation(), Sound.ENDERMAN_HIT, 1f, 1f);
                        }).setExecute(ClickType.RIGHT, o1 -> {
                            templateBlock.addYaw();
                            editor.getPlayer().playSound(editor.getPlayer().getLocation(), Sound.ORB_PICKUP, 1f, 1f);
                        })
                );
            }
        }
        guiMenu.setItem(35,new GuiItem(
                Utils.buildItem(
                        new ItemStack(Material.REDSTONE_BLOCK),
                        "&c&lREMOVE",
                        Collections.singletonList(
                                "&eRemove this spawn"
                        ),
                        null
                )).setExecute(ClickType.LEFT, o -> {
                    spawns.remove(templateBlock.getTeamNumber());
                    spawnChests.remove(templateBlock.getTeamNumber());
                    editor.getPlayer().sendMessage("&6&lREMOVED!&r&e Team "+ templateBlock.getTeamNumber() +"'s spawn has been removed!");
                    templateBlock.destroy();
                }
        ));
    }

    private static final InteractiveItem teleportCompass = new InteractiveItem(
            Utils.buildItem(Material.COMPASS, "&b&lTELEPORT COMPASS",
                    Arrays.asList(
                            "&7Left click to target block",
                            "&7to &ateleport&7 to it's location"
                    ),
                    null)
    ).setInteract(Arrays.asList(Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK), event -> {
        event.setCancelled(true);
        Player player = event.getPlayer();
        Location loc = Utils.getTargetBlockLocation(player);
        player.teleport(loc);
        player.playSound(loc, Sound.ENDERMAN_TELEPORT, 1f, 1f);
    });

    private static final InteractiveItem spawnBeacon = new InteractiveItem(
            Utils.buildItem(Material.BEACON, "&a&lADD SPAWN",
                    Collections.singletonList("&7Place to add spawn location"),
                    null
            )
    ).setInteract(Action.RIGHT_CLICK_BLOCK, event -> {
        ArenaSetup arenaSetup = ArenaSetupManager.getByEditor(GamePlayer.getGamePlayer(event.getPlayer()));
        if (arenaSetup == null) return;

        event.setCancelled(true);
        Location loc = event.getClickedBlock().getLocation().add(0,1,0);
        loc.setYaw(Utils.getAbsoluteYaw(loc.getYaw()));
        TemplateArena.TemplateLocation templateLocation = new TemplateArena.TemplateLocation(loc);

        if (arenaSetup.getSpawns().size() == MAX_SPAWNS) {
            arenaSetup.getEditor().getPlayer().sendMessage("&4&lERROR! &r&cReached maximum team!");
            return;
        }
        for (int i = 1; i<=MAX_SPAWNS; ++i) {
            if (!arenaSetup.getSpawns().containsKey(i) || arenaSetup.getSpawns().get(i) == null) {
                arenaSetup.setCurrentSpawn(i);
                arenaSetup.getSpawns().put(i,templateLocation);
                arenaSetup.getSpawnChests().put(i, new ArrayList<>());
                arenaSetup.getWorld().getBlockAt(loc).setType(Material.BEACON);

                TemplateBlock templateBlock = new TemplateBlock(loc, i);
                templateBlock.setConsumer(e -> {
                    arenaSetup.openSpawnsMenu(templateBlock);
                });
                break;
            }
        }
    });
    private static final InteractiveItem chestStick = new InteractiveItem(
            Utils.buildItem(
                    Material.STICK,
                    "&e&lADD CHEST",
                    Arrays.asList(
                            "&eRight click&7 to chest to",
                            "&7set it as spawn's chests",
                            "",
                            "&eLeft click&7 to remove chest"
                    ),
                    null
            ))
            .setInteract(Action.RIGHT_CLICK_BLOCK, event -> {
                event.setCancelled(true);

                ArenaSetup arenaSetup = ArenaSetupManager.getByEditor(GamePlayer.getGamePlayer(event.getPlayer()));
                if (arenaSetup == null) return;

                if (arenaSetup.getCurrentSpawn() == 0 || arenaSetup.getCurrentSpawn() > MAX_SPAWNS) {
                    arenaSetup.getEditor().sendMessage("&4&lERROR!&r&c No spawn provided!");
                    return;
                }

                if (arenaSetup.getSpawnChests().get(arenaSetup.getCurrentSpawn()).size() == CHEST_PER_SPAWN) {
                    arenaSetup.getEditor().sendMessage("&4&lERROR!&r&c Reached maximum chests per spawn!");
                    return;
                }

                Block block = event.getClickedBlock();
                if (block == null || !block.getType().equals(Material.CHEST)) return;

                TemplateBlock templateBlock = new TemplateBlock(block.getLocation(), arenaSetup.getCurrentSpawn());
                TemplateArena.TemplateLocation templateLocation = templateBlock.toTemplateLocation();

                for (List<TemplateArena.TemplateLocation> locations : arenaSetup.getSpawnChests().values()) {
                    if (locations.contains(templateLocation)) {
                        arenaSetup.getEditor().sendMessage("&4&lERROR!&r&c This chest has been added to a team");
                        return;
                    }
                }
                if (arenaSetup.getMidChests().contains(templateLocation)) {
                    arenaSetup.getEditor().sendMessage("&4&lERROR!&r&c This chest had already a mid chest!");
                    return;
                }
                templateBlock.getHologram().setLines(
                        "&aTeam #" + arenaSetup.getCurrentSpawn() +"'s chest",
                        "",
                        "&eLeft click to remove!");
                templateBlock.setConsumer(e -> {
                    if (arenaSetup.getSpawnChests().containsKey(templateBlock.getTeamNumber()) &&
                            arenaSetup.getSpawnChests().get(templateBlock.getTeamNumber()).contains(templateLocation)) {

                        arenaSetup.getSpawnChests().get(templateBlock.getTeamNumber()).remove(templateLocation);
                        arenaSetup.getEditor().sendMessage("&2&lREMOVED!&r&aa A chest in &eteam " + templateBlock.getTeamNumber() +" &ahas been removed!");
                        templateBlock.destroy();
                    }
                });

                arenaSetup.getSpawnChests().get(arenaSetup.getCurrentSpawn()).add(templateBlock.toTemplateLocation());
    });
    private static final InteractiveItem midChestStick = new InteractiveItem(
            Utils.buildItem(Material.BLAZE_ROD, "&6&lMID CHEST ADDER",
                    Arrays.asList(
                            "&7Right click to chest at mid",
                            "&7to add it to mid chest list.",
                            "",
                            "&7Left click to remove it."
                    ),
                    null)
    ).setInteract(Action.RIGHT_CLICK_BLOCK, event -> {
        ArenaSetup arenaSetup = ArenaSetupManager.getByEditor(GamePlayer.getGamePlayer(event.getPlayer()));
        if (arenaSetup == null) return;

        Block block = event.getClickedBlock();
        if (block == null || !block.getType().equals(Material.CHEST)) return;

        TemplateBlock templateBlock = new TemplateBlock(block.getLocation(), -1);
        TemplateArena.TemplateLocation templateLocation = templateBlock.toTemplateLocation();

        for (List<TemplateArena.TemplateLocation> locations : arenaSetup.getSpawnChests().values()) {
            if (locations.contains(templateLocation)) {
                arenaSetup.getEditor().sendMessage("&4&lERROR!&r&c This chest has been added to a team");
                return;
            }
        }

        if (arenaSetup.getMidChests().contains(templateLocation)) {
            arenaSetup.getEditor().sendMessage("&4&lERROR!&r&c This chest had already a mid chest!");
            return;
        }
        templateBlock.getHologram().setLines(
                "&aMid chest",
                "",
                "&eLeft click to remove!");
        templateBlock.setConsumer(e -> {
            event.setCancelled(true);
            if (arenaSetup.getMidChests().contains(templateLocation)) {
                arenaSetup.getMidChests().remove(templateLocation);
                templateBlock.destroy();
                arenaSetup.getEditor().sendMessage("&2&lREMOVED!&r&a A mid chest has been removed!");
            }
        });
        arenaSetup.getMidChests().add(templateLocation);
    });
    private static final InteractiveItem cancelItem = new InteractiveItem(
            Utils.buildItem(
                    Material.WOOL,
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
    private static final InteractiveItem nextStageItem = new InteractiveItem(
            Utils.buildItem(
                    Material.WOOL,
                    "&2&lNEXT STAGE",
                    Collections.emptyList(),
                    null
            ))
            .setInteract(
                    Arrays.asList(Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR), event -> {
                        event.setCancelled(true);

                        ArenaSetup arenaSetup = ArenaSetupManager.getByEditor(GamePlayer.getGamePlayer(event.getPlayer()));
                        if (arenaSetup == null) return;

                        boolean valid = true;

                        for (int i = 1; i <= MAX_SPAWNS; ++i) {
                            if (!arenaSetup.getSpawns().containsKey(i)) {
                                arenaSetup.getEditor().sendMessage("&4&lERROR!&r&c Team " +i +"'s spawn hasn't been set");
                                valid = false;
                            }
                            if (arenaSetup.getSpawnChests().get(i).size() != CHEST_PER_SPAWN) {
                                arenaSetup.getEditor().sendMessage("&4&lERROR!&r&c Team " +i +"'s chests number hasn't reached " + CHEST_PER_SPAWN);
                                valid = false;
                            }
                        }

                        if (!valid) return;

                        arenaSetup.setStage(ArenaSetupStage.MID);
                    });
    private static final InteractiveItem previousStageItem = new InteractiveItem(
            Utils.buildItem(
                    Material.WOOL,
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
    private static final InteractiveItem saveItem = new InteractiveItem(
            Utils.buildItem(
                    Material.WOOL,
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

                        FileConfig fileConfig = new FileConfig("maps/" + arenaSetup.getMapName() + ".yml");
                        fileConfig.addDefault("name",Utils.toBetterName(arenaSetup.getMapName()));

                        List<Map<String, Object>> spawnsData = new ArrayList<>();
                        for (int i = 1; i <= MAX_SPAWNS; ++i) {
                            Map<String, Object> spawnData = new HashMap<>();
                            List<Map<String, Object>> spawnChestsData = new ArrayList<>();

                            TemplateArena.TemplateLocation spawnLocation = arenaSetup.getSpawns().get(i);
                            spawnData.put("x", spawnLocation.getX());
                            spawnData.put("y", spawnLocation.getY());
                            spawnData.put("z", spawnLocation.getZ());
                            spawnData.put("yaw", spawnLocation.getYaw());
                            spawnData.put("pitch", spawnLocation.getPitch());

                            for (TemplateArena.TemplateLocation chestLocation : arenaSetup.getSpawnChests().get(i)) {
                                Map<String, Object> chestData = new HashMap<>();
                                chestData.put("x", chestLocation.getX());
                                chestData.put("y", chestLocation.getY());
                                chestData.put("z", chestLocation.getZ());
                                spawnChestsData.add(chestData);
                            }
                            spawnData.put("chests", spawnChestsData);
                            spawnsData.add(spawnData);
                        }

                        List<Map<String, Object>> midChestsData = new ArrayList<>();
                        for (int i = 0; i < arenaSetup.getMidChests().size(); ++i) {
                            Map<String, Object> midChestData = new HashMap<>();
                            TemplateArena.TemplateLocation chestLocation = arenaSetup.getMidChests().get(i);
                            midChestData.put("x", chestLocation.getX());
                            midChestData.put("y", chestLocation.getY());
                            midChestData.put("z", chestLocation.getZ());
                            midChestsData.add(midChestData);
                        }

                        fileConfig.getConfig().set("spawns", spawnsData);
                        fileConfig.getConfig().set("midChests", midChestsData);

                        fileConfig.removeFile(); 
                        fileConfig.saveConfig();

                        TemplateBlock.removeWorld(arenaSetup.getWorld());
                    });
}
