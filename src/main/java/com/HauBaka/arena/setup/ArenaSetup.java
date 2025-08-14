package com.HauBaka.arena.setup;

import com.HauBaka.arena.TemplateArena;
import com.HauBaka.enums.ArenaSetupStage;
import com.HauBaka.menu.GuiItem;
import com.HauBaka.menu.GuiMenu;
import com.HauBaka.object.Hologram;
import com.HauBaka.object.InteractiveItem;
import com.HauBaka.player.GamePlayer;
import com.HauBaka.utils.Utils;
import com.HauBaka.world.WorldManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
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
        private static Map<World, List<TemplateBlock>> listBlocks;
        @Getter
        private final int x;
        @Getter
        private final int y;
        @Getter
        private final int z;
        @Getter
        private Hologram hologram;
        Direction direction;
        @Getter
        private int teamNumber;
        @Setter
        private Consumer<BlockBreakEvent> consumer;
        private Location loc;
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

            if (!listBlocks.containsKey(loc.getWorld())) listBlocks.put(loc.getWorld(), Arrays.asList(this));
            else listBlocks.get(loc.getWorld()).add(this);
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
    private final String map;
    @Getter
    private World world;
    @Getter
    private ArenaSetupStage stage;
    @Getter
    private final Map<Integer, TemplateArena.TemplateLocation> spawns;
    @Getter
    private final Map<Integer, List<TemplateArena.TemplateLocation>> spawnChests;
    @Getter
    private Map<Integer, ItemStack> oldInventory;
    public ArenaSetup(String map,  GamePlayer editor) {
        this.map = map;
        this.editor = editor;
        this.spawns = new HashMap<>();
        this.spawnChests = new HashMap<>();
        this.stage = ArenaSetupStage.SPAWN;
        WorldManager.cloneWorld(this.map, w -> {
            this.world = w;
            joinEdit();
        });
    }
    public void joinEdit() {
        editor.getPlayer().teleport(this.world.getSpawnLocation());
        setItems();
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
        inventory.setItem(2, spawnBeacon.getItem());
        inventory.setItem(3,chestStick.getItem());
        inventory.setItem(8, cancelItem.getItem());
        switch (stage) {
            case SPAWN:
                inventory.setItem(5, nextStageItem.getItem());
                break;
            case MID:
                inventory.setItem(5, previousStageItem.getItem());
                inventory.setItem(7, saveItem.getItem());
                break;
        }
    }

    static final int[] invLocations = {10,11,12,13,14,15,16,19,20,21,22,23};
    public void openSpawnsMenu(TemplateBlock templateBlock) {
        GuiMenu guiMenu = new GuiMenu(Utils.toBetterName(map) +"'s spawns", 36, editor);
        for (int i = 0; i < MAX_SPAWNS; ++i) {
            int team = i+1;
            if (team != templateBlock.getTeamNumber()) {
                guiMenu.setItem(invLocations[i], new GuiItem(
                        Utils.buildItem(
                                new ItemStack(Material.WOOL, team, (byte) (spawns.get(team) == null ? 7 : 5)),
                                "&aTeam " + team,
                                Arrays.asList("&eClick to set current", "&elocation to &ateam " + team + "'s", "&espawn&e."),
                                null),
                        o -> {
                            spawns.put(team, templateBlock.toTemplateLocation());
                            spawnChests.put(team, spawnChests.getOrDefault(templateBlock.getTeamNumber(),new ArrayList<>()));

                            spawns.remove(templateBlock.getTeamNumber());
                            spawnChests.remove(templateBlock.getTeamNumber());

                            templateBlock.setTeam(team);

                            guiMenu.setItem(invLocations[team-1], new GuiItem(
                                    Utils.buildItem(
                                            new ItemStack(Material.WOOL, team, (byte) 14),
                                            "&aTeam" + team,
                                            Arrays.asList("&eThis is current location!"),
                                            null
                                    ),
                                    o1 -> {
                                        editor.getPlayer().sendMessage("&4&lERROR &cTeam " + team +"'s spawn is current location!");
                                        editor.getPlayer().playSound(editor.getPlayer().getLocation()
                                        , Sound.ENDERMAN_HIT, 1f, 1f);
                                    }
                            ));
                        }
                ));
            } else {
                guiMenu.setItem(invLocations[team-1], new GuiItem(
                        Utils.buildItem(
                                new ItemStack(Material.WOOL, team, (byte) 14),
                                "&aTeam" + team,
                                Arrays.asList("&eThis is current location!"),
                                null
                        ),
                        o -> {
                            editor.getPlayer().sendMessage("&4&lERROR &cTeam " + team +"'s spawn is current location!");
                            editor.getPlayer().playSound(editor.getPlayer().getLocation()
                                    , Sound.ENDERMAN_HIT, 1f, 1f);
                        }
                ));
            }
        }
        guiMenu.setItem(35,new GuiItem(
                Utils.buildItem(
                        new ItemStack(Material.REDSTONE_BLOCK),
                        "&c&lREMOVE",
                        Arrays.asList(
                                "&eRemove this spawn"
                        ),
                        null
                ),
                o -> {
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
                    Arrays.asList("&7Place to add spawn location"),
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
                            "&7Right click to chest to",
                            "&7set it as spawn's chests"
                    ),
                    null
            ))
            .setInteract(Action.RIGHT_CLICK_BLOCK, event -> {
                event.setCancelled(true);
    });
    private static final InteractiveItem cancelItem = new InteractiveItem(
            Utils.buildItem(
                    Material.WOOL,
                    "&c&lCANCEL",
                    Arrays.asList(),
                    null
            ))
            .setInteract(Arrays.asList(Action.values()), event -> {
                event.setCancelled(true);
    });
    private static final InteractiveItem nextStageItem = new InteractiveItem(
            Utils.buildItem(
                    Material.WOOL,
                    "&2&lNEXT STAGE",
                    Arrays.asList(),
                    null
            ))
            .setInteract(
                    Arrays.asList(Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR), event -> {
                        event.setCancelled(true);
                    });
    private static final InteractiveItem previousStageItem = new InteractiveItem(
            Utils.buildItem(
                    Material.WOOL,
                    "&2&lPREVIOUS STAGE",
                    Arrays.asList(),
                    null
            ))
            .setInteract(
                    Arrays.asList(Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR),
                    event -> {
                        event.setCancelled(true);
                    });
    private static final InteractiveItem saveItem = new InteractiveItem(
            Utils.buildItem(
                    Material.WOOL,
                    "&a&lSAVE",
                    Arrays.asList(),
                    null
            ))
            .setInteract(
                    Arrays.asList(Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR),
                    event -> {
                        event.setCancelled(true);
                    });
}
