package com.HauBaka.arena.setup;

import com.HauBaka.arena.TemplateArena;
import com.HauBaka.enums.ArenaSetupStage;
import com.HauBaka.file.FileConfig;
import com.HauBaka.menu.GuiItem;
import com.HauBaka.menu.GuiMenu;
import com.HauBaka.object.GameScoreboard;
import com.HauBaka.object.Hologram;
import com.HauBaka.object.TemplateBlock;
import com.HauBaka.player.GamePlayer;
import com.HauBaka.utils.Utils;
import com.HauBaka.world.WorldManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class ArenaSetup {
    public static final int MAX_SPAWNS = 12;
    public static final int CHEST_PER_SPAWN = 3;
    @Getter
    private final GamePlayer editor;
    @Getter
    private final String mapName;
    @Getter
    private World world;
    @Getter
    private ArenaSetupStage stage;
    @Getter
    private final Map<Integer, TemplateBlock> spawns;
    @Getter
    private final Map<Integer, List<TemplateBlock>> spawnChests;
    @Getter
    private final List<TemplateBlock> midChests;
    @Getter
    private Map<Integer, ItemStack> oldInventory;
    @Getter @Setter
    private int currentSpawn;
    @Getter
    private TemplateBlock lobby;
    public ArenaSetup(String mapName, GamePlayer editor) {
        this.mapName = mapName.toLowerCase();
        this.editor = editor;
        this.spawns = new HashMap<>();
        this.spawnChests = new HashMap<>();
        this.midChests = new ArrayList<>();
        this.currentSpawn = 0;
        this.editor.sendMessage("&6&lINFO&r&e Setting up map...");
        WorldManager.cloneWorld(this.mapName, w -> {
            this.world = w;
            loadFile();
            joinEdit();
        });
    }
    private void loadFile() {
        FileConfig file = new FileConfig("/maps/"+mapName+".yml");
        if (!file.getFile().exists()) {
            editor.sendMessage("&6&lINFO&r&e No setup file found!");
            return;
        }
        TemplateArena templateArena = new TemplateArena(mapName);
        templateArena.setUp(this);
    }
    public void joinEdit() {
        this.editor.sendMessage("&6&lINFO&r&e Sending you to &a" + world.getName());
        editor.getPlayer().teleport(this.world.getSpawnLocation());
        setStage(ArenaSetupStage.SPAWN);
        updateScoreboard();
    }
    private void updateScoreboard() {
        GameScoreboard scoreboard = this.editor.getScoreboard();
        scoreboard.setTitle("&f Map name: &a" + mapName);
        List<String> contents = new ArrayList<>();
        contents.add("&0&l•&r&f Lobby: " + (lobby == null ? "&c&l✗" : "&a&l✔"));
        contents.add("&0&l•&r&f Mid chests: &a" + midChests.size());
        contents.add(" Team | Chests ");
        for (int i = 1; i <= MAX_SPAWNS; ++i) {
            String index = Utils.toDecimalFormat(i,"00");
            if (spawns.containsKey(i)) {
                contents.add("#" + index + "  &a&l✔&r  |  &a" + spawnChests.get(i).size());
            } else {
                contents.add("#" + index + "  &c&l✗&r  |  &7" + 0);
            }
        }
        scoreboard.setContents(contents);
    }
    public void setStage(ArenaSetupStage stage) {
        this.stage = stage;
        setItems();
    }
    public void stopEdit() {
        this.editor.getPlayer().getInventory().clear();
        for (int slot : oldInventory.keySet())
            this.editor.getPlayer().getInventory().setItem(slot, oldInventory.get(slot));
        if (lobby != null) {
            world.getBlockAt(lobby.getX(), lobby.getY(), lobby.getZ()).setType(lobby.getOldMaterial());
            lobby.destroy();
        }
        for (TemplateBlock spawn : spawns.values()) {
            world.getBlockAt(spawn.getX(), spawn.getY(), spawn.getZ()).setType(spawn.getOldMaterial());
            spawn.destroy();
        }
        for (List<TemplateBlock> chests : spawnChests.values())
            for (TemplateBlock chest : chests) chest.destroy();
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
        inventory.setItem(0, ArenaSetupItems.teleportCompass.getItem());
        inventory.setItem(1, ArenaSetupItems.lobbyPortal.getItem());
        inventory.setItem(8, ArenaSetupItems.cancelItem.getItem());
        switch (stage) {
            case SPAWN:
                inventory.setItem(2, ArenaSetupItems.spawnBeacon.getItem());
                inventory.setItem(3, ArenaSetupItems.chestStick.getItem());
                inventory.setItem(5, ArenaSetupItems.nextStageItem.getItem());
                break;
            case MID:
                inventory.setItem(3, ArenaSetupItems.midChestStick.getItem());
                inventory.setItem(5, ArenaSetupItems.previousStageItem.getItem());
                inventory.setItem(7, ArenaSetupItems.saveItem.getItem());
                break;
        }
    }
    public void setLobby(Location loc) {
        if (loc == null) {
            if (lobby == null) return;
            Block block = world.getBlockAt(lobby.getX(), lobby.getY(), lobby.getZ());
            if (block != null) {
                block.setType(lobby.getOldMaterial());
            }
            lobby.destroy();
            lobby = null;
            editor.sendMessage("&6&lREMOVED!&r&e Lobby has been removed!");
            updateScoreboard();
            return;
        }
        if (this.lobby != null) {
            editor.sendMessage("&4&lERROR!&r&c Lobby has been provided!");
            return;
        }
        this.lobby = new TemplateBlock(loc, -1);
        Block block = loc.getBlock();
        lobby.setOldMaterial(block.getType());
        block.setType(Material.ENDER_PORTAL_FRAME);
        lobby.getHologram().setLines(
                "&6&lLOBBY",
                "",
                "&e&lLEFT CLICK TO REMOVE"
        );
        lobby.setConsumer(Action.LEFT_CLICK_BLOCK,o -> {
            setLobby((Location) null);
        });
        editor.sendMessage("&2&lPLACED!&r&a Lobby has been set!");
        updateScoreboard();
    }
    public void addTeam(Location loc) {
        if (getSpawns().size() == MAX_SPAWNS) {
            getEditor().sendMessage("&4&lERROR! &r&cReached maximum team!");
            return;
        }

        for (int i = 1;  i<= MAX_SPAWNS; ++i) {
            if (!getSpawns().containsKey(i) || getSpawns().get(i) == null) {
                TemplateBlock templateBlock = new TemplateBlock(loc, i);

                Block block = getWorld().getBlockAt(loc);
                templateBlock.setOldMaterial(block == null ? Material.AIR : block.getType());
                loc.getBlock().setType(Material.BEACON);

                setCurrentSpawn(i);
                getSpawns().put(i, templateBlock);
                getSpawnChests().put(i, new ArrayList<>());

                addTeamBlock(templateBlock);
                break;
            }
        }
        updateScoreboard();
    }
    private void addTeamBlock(TemplateBlock block) {
        Hologram hologram = block.getHologram();
        hologram.setLines(
                "&aTeam " + block.getTeamNumber() +"'s spawn",
                "&eDirection: &a" + block.getDirection().toString(),
                "",
                "&e&lRIGHT CLICK TO CHANGE!",
                "&e&lLEFT CLICK TO REMOVE!"
        );

        block.setConsumer(Action.RIGHT_CLICK_BLOCK,e -> {
            openSpawnsMenu(block);
        });
        block.setConsumer(Action.LEFT_CLICK_BLOCK, e -> {
            removeTeam(block);
        });
    }
    public void removeTeam(TemplateBlock spawnBeacon) {
        if (spawnBeacon == null) return;

        int teamNumber = spawnBeacon.getTeamNumber();
        if (spawns.containsKey(teamNumber)) {
            for (TemplateBlock chest : getSpawnChests().get(teamNumber))
                chest.destroy();
            getSpawnChests().remove(teamNumber);

            spawns.get(teamNumber).destroy();
            spawns.remove(teamNumber);

            world.getBlockAt(spawnBeacon.getX(), spawnBeacon.getY(), spawnBeacon.getZ()).setType(spawnBeacon.getOldMaterial());
            editor.sendMessage("&2&lREMOVED! &r&aTeam #" + teamNumber +" &ehas been removed!");
            editor.getPlayer().playSound(editor.getPlayer().getLocation(), Sound.SLIME_WALK, 1f, 1f);
        }
        setCurrentSpawn(0);
        updateScoreboard();
    }
    /**
     * Replace team A by team B and then remove B
     * @param teamA
     * @param teamB
     * @param removeChest true if only replace A's spawn with B's spawn
     */
    public void replaceTeam(int teamA, int teamB, boolean removeChest) {
        if (!spawns.containsKey(teamB)) return;
        //remove team A's chests
        if (spawns.containsKey(teamA)) {
            for (TemplateBlock chest : spawnChests.get(teamA)) //remove A' chests
                chest.destroy();

            TemplateBlock spawnBeacon = spawns.get(teamA);
            world.getBlockAt(spawnBeacon.getX(), spawnBeacon.getY(), spawnBeacon.getZ()).setType(spawnBeacon.getOldMaterial());
            spawnBeacon.destroy();
        }
        //replace A with B
        spawns.get(teamB).setTeam(teamA);
        spawns.put(teamA, spawns.get(teamB));
        spawnChests.put(teamA, new ArrayList<>());
        if (!removeChest) {
            for (TemplateBlock chest : spawnChests.get(teamB)) {
                chest.setTeam(teamA);
                chest.getHologram().setLines(
                        "&aTeam #" + teamA +"'s chest",
                        "",
                        "&eLeft click to remove!"
                );
            }
            spawnChests.put(teamA, spawnChests.get(teamB));
        }
        //remove B
        spawns.remove(teamB);
        if (removeChest) for (TemplateBlock chest : spawnChests.get(teamB)) chest.destroy();
        spawnChests.remove(teamB);

        setCurrentSpawn(teamA);
        editor.sendMessage("&3&lREPLACED!&r&a Team #" + teamB + "&r&e has been replaced by &aTeam #" + teamA);
        editor.getPlayer().playSound(editor.getPlayer().getLocation(), Sound.ORB_PICKUP, 1f, 1f);
    }
    public void addChest(Block block, ArenaSetupStage stage) {
        TemplateBlock templateBlock = new TemplateBlock(block.getLocation(), -1);

        for (List<TemplateBlock> locations : getSpawnChests().values()) {
            for (TemplateBlock location : locations) {
                if (location.equals(templateBlock)) {
                    getEditor().sendMessage("&4&lERROR!&r&c This chest has been added to a team");
                    return;
                }
            }
        }
        for (TemplateBlock location : getMidChests()) {
            if (location.equals(templateBlock)) {
                getEditor().sendMessage("&4&lERROR!&r&c This chest had already a mid chest!");
                return;
            }
        }
        if (stage == ArenaSetupStage.SPAWN) {
            templateBlock.getHologram().setLines(
                    "&aTeam #" + getCurrentSpawn() +"'s chest",
                    "",
                    "&eLeft click to remove!");

            templateBlock.setConsumer(Action.LEFT_CLICK_BLOCK, e -> {
                if (!getSpawnChests().containsKey(templateBlock.getTeamNumber())) {
                    return;
                }
                for (TemplateBlock tpl : getSpawnChests().get(templateBlock.getTeamNumber())) {
                    if (tpl == templateBlock) {
                        getSpawnChests().get(templateBlock.getTeamNumber()).remove(tpl);
                        getEditor().sendMessage("&6&lREMOVED! &r&eA chest in &ateam #" + templateBlock.getTeamNumber() +" &ehas been removed!");
                        templateBlock.destroy();
                        updateScoreboard();
                        return;
                    }
                }
            });
            getSpawnChests().get(getCurrentSpawn()).add(templateBlock);
            updateScoreboard();
            return;
        }
        templateBlock.getHologram().setLines(
                "&aMid chest",
                "",
                "&eLeft click to remove!");
        templateBlock.setConsumer(Action.LEFT_CLICK_BLOCK, e -> {
            if (getMidChests().contains(templateBlock)) {
                getMidChests().remove(templateBlock);
                templateBlock.destroy();
                getEditor().sendMessage("&2&lREMOVED!&r&a A mid chest has been removed!");
                updateScoreboard();
            }
        });
        getMidChests().add(templateBlock);
        updateScoreboard();
    }
    public boolean isValid() {
        boolean valid = true;
        for (int i = 1; i <= MAX_SPAWNS; ++i) {
            if (!getSpawns().containsKey(i)) {
                getEditor().sendMessage("&4&lERROR!&r&a Team #" +i +"'s spawn&c hasn't been set");
                valid = false;
            }
            if (getSpawnChests().containsKey(i) && getSpawnChests().get(i).size() != CHEST_PER_SPAWN) {
                getEditor().sendMessage("&4&lERROR!&r&a Team #" +i +"'s chests number&c hasn't reached " + CHEST_PER_SPAWN);
                valid = false;
            }
        }
        return valid;
    }

    static final int[] invLocations = {10,11,12,13,14,15,16,19,20,21,22,23};
    public void openSpawnsMenu(TemplateBlock templateBlock) {
        GuiMenu guiMenu = new GuiMenu(Utils.toBetterName(mapName) +"'s spawns", 36, editor);
        for (int i = 0; i < MAX_SPAWNS; ++i) {
            if (i+1 != templateBlock.getTeamNumber()) {
                TemplateBlock team = spawns.get(i+1);
                if (team == null) {
                    int finalI = i;
                    guiMenu.setItem(invLocations[i], new GuiItem(
                                    Utils.buildItem(
                                            new ItemStack(Material.WOOL, i+1, (byte) 7),
                                            "&aTeam #" + (i+1),
                                            Arrays.asList(
                                                    "&eLeft click&7 to set current",
                                                    "&7location to &ateam #" + (i+1) + "'s",
                                                    "&aspawn&7.",
                                                    "",
                                                    "&4&lWarning&r&c Chests in this team will",
                                                    "&cbe removed!."
                                            ),
                                            null)
                            ).setExecute(ClickType.LEFT,o -> {
                                replaceTeam(finalI +1, templateBlock.getTeamNumber(), false);
                                editor.getPlayer().closeInventory();
                                openSpawnsMenu(templateBlock);
                            })
                    );
                } else {
                    guiMenu.setItem(invLocations[i], new GuiItem(
                                    Utils.buildItem(
                                            new ItemStack(Material.WOOL, team.getTeamNumber(), (byte) 5),
                                            "&aTeam #" + team.getTeamNumber(),
                                            Arrays.asList(
                                                    "&eLeft click&7 to set current",
                                                    "&7location to &ateam #" + team.getTeamNumber() + "'s",
                                                    "&aspawn&7.",
                                                    "",
                                                    "&4&lWarning&r&c Chests in this team will",
                                                    "&cbe removed!.",
                                                    "",
                                                    "&eRight click&7 to &arotate yaw.",
                                                    "",
                                                    "&eMiddle click&7 to teleport to."
                                            ),
                                            null)
                            ).setExecute(ClickType.LEFT,o -> {
                                replaceTeam(team.getTeamNumber(), templateBlock.getTeamNumber(), false);
                                editor.getPlayer().closeInventory();
                                openSpawnsMenu(templateBlock);
                            }).setExecute(ClickType.RIGHT, o -> {
                                team.addYaw();
                                editor.getPlayer().playSound(editor.getPlayer().getLocation(), Sound.ORB_PICKUP, 1f, 1f);
                            }).setExecute(ClickType.MIDDLE, o -> {
                                Location loc = world.getSpawnLocation();
                                loc.setX(team.getX());
                                loc.setY(team.getY());
                                loc.setZ(team.getZ());
                                loc.setYaw(team.getDirection().getYaw());
                                loc.setPitch(editor.getPlayer().getLocation().getPitch());
                                editor.getPlayer().teleport(loc);
                                editor.getPlayer().playSound(loc,Sound.ENDERMAN_TELEPORT, 1f, 1f);
                            })
                    );
                }
            } else {
                guiMenu.setItem(
                        invLocations[i],
                        new GuiItem(
                            Utils.buildItem(
                                    new ItemStack(Material.WOOL, i+1, (byte) 14),
                                    "&aTeam #" + (i+1),
                                    Arrays.asList(
                                            "&cThis is current location!",
                                            "",
                                            "&eRight click&7 to &arotate yaw."
                                    ),
                                    null
                            )
                        ).setExecute(ClickType.LEFT, o-> {
                            editor.sendMessage("&4&lERROR! &cTeam " + templateBlock.getTeamNumber() +"'s spawn is current location!");
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
                                "&eRemove this spawn."
                        ),
                        null
                )).setExecute(ClickType.LEFT, o -> {
                    removeTeam(templateBlock);
                    editor.getPlayer().closeInventory();
                }
        ));
        guiMenu.open();
    }


    public void save() {
        if (!isValid()) return;

        FileConfig fileConfig = new FileConfig("maps/" + getMapName() + ".yml", true);
        fileConfig.addDefault("name",Utils.toBetterName(getMapName()));

        List<Map<String, Object>> spawnsData = new ArrayList<>();
        for (int i = 1; i <= MAX_SPAWNS; ++i) {
            Map<String, Object> spawnData = new HashMap<>();
            List<Map<String, Object>> spawnChestsData = new ArrayList<>();

            TemplateBlock spawnLocation = getSpawns().get(i);
            spawnData.put("x", spawnLocation.getX());
            spawnData.put("y", spawnLocation.getY());
            spawnData.put("z", spawnLocation.getZ());
            spawnData.put("yaw", spawnLocation.getDirection().getYaw());
            spawnData.put("pitch", 0);

            for (TemplateBlock chestLocation : getSpawnChests().get(i)) {
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
        for (int i = 0; i < getMidChests().size(); ++i) {
            Map<String, Object> midChestData = new HashMap<>();
            TemplateBlock chestLocation = getMidChests().get(i);
            midChestData.put("x", chestLocation.getX());
            midChestData.put("y", chestLocation.getY());
            midChestData.put("z", chestLocation.getZ());
            midChestsData.add(midChestData);
        }

        if (lobby != null) {
            fileConfig.getConfig().set("lobby.x", lobby.getX());
            fileConfig.getConfig().set("lobby.y", lobby.getY());
            fileConfig.getConfig().set("lobby.z", lobby.getZ());
        }
        fileConfig.getConfig().set("spawns", spawnsData);
        fileConfig.getConfig().set("midChests", midChestsData);

        fileConfig.removeFile();
        fileConfig.saveConfig();

        stopEdit();
    }
}
