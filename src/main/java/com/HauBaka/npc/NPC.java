package com.HauBaka.npc;

import com.HauBaka.Skywars;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.scoreboard.CraftScoreboard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.InputStreamReader;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

public class NPC implements Listener {
    @Getter
    private final NPCType type;
    @Getter
    private final Entity npc;
    @Setter
    private Consumer<Player> clickEvent;
    @Getter
    private final String name;
    @Getter
    private String skinName;
    @Getter @Setter
    private String cmd;
    NPC(Location loc, NPCType type, String name) {
        if (name == null || name.isEmpty()) name = NPCManager.generateNPCName();
        this.name = name;
        try {
            this.type =  type;
            WorldServer nmsWorld = ((CraftWorld) loc.getWorld()).getHandle();
            if (type == NPCType.PLAYER) {
                npc = new EntityPlayer(
                        MinecraftServer.getServer(),
                        nmsWorld,
                        new GameProfile(UUID.randomUUID(), name),
                        new PlayerInteractManager(nmsWorld)
                );
                ((EntityPlayer) npc).playerConnection = new PlayerConnection(((CraftServer) Bukkit.getServer()).getServer(), new NetworkManager(EnumProtocolDirection.CLIENTBOUND), (EntityPlayer) npc);
                ((EntityPlayer) npc).playerConnection.getPlayer().getPlayer().setPlayerListName("§8[NPC] " + name);
            } else {
                npc = type.getClazz().getConstructor(World.class).newInstance(nmsWorld);
            }
            npc.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
            npc.setCustomNameVisible(false);
            if (npc instanceof EntitySkeleton) {
                ((EntitySkeleton) npc).setSkeletonType(type == NPCType.WITHER_SKELETON ? 1 : 0);
            }
            nmsWorld.addEntity(npc);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Bukkit.getPluginManager().registerEvents(this, Skywars.getInstance());
    }

    private void hideFromTablist(Player player) {
        ScoreboardTeam team = new ScoreboardTeam(((CraftScoreboard) Bukkit.getScoreboardManager().getMainScoreboard()).getHandle(),name);
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        team.setNameTagVisibility(ScoreboardTeamBase.EnumNameTagVisibility.NEVER);

        connection.sendPacket(new PacketPlayOutScoreboardTeam(team, 1));
        connection.sendPacket(new PacketPlayOutScoreboardTeam(team, 0));
        connection.sendPacket(new PacketPlayOutScoreboardTeam(team, new ArrayList<String>(){{add(name);}}, 3));

        new BukkitRunnable() {
            @Override
            public void run() {

                DataWatcher dw = new DataWatcher(null);
                dw.a(10, (byte) (0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40));
                PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(npc.getId(), dw, true);
                connection.sendPacket(packet);

                connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, (EntityPlayer) npc));
            }
        }.runTaskLaterAsynchronously(Skywars.getInstance(), 10);
    }
    public void setSkin(String username) {
        if (type != NPCType.PLAYER) return;
        this.skinName = username;
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    HttpsURLConnection connection = (HttpsURLConnection) new URL(String.format("https://api.ashcon.app/mojang/v2/user/%s", username)).openConnection();
                    if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                        ArrayList<String> lines = new ArrayList<>();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        reader.lines().forEach(lines::add);

                        String reply = String.join(" ",lines);
                        int indexOfValue = reply.indexOf("\"value\": \"");
                        int indexOfSignature = reply.indexOf("\"signature\": \"");
                        String skin = reply.substring(indexOfValue + 10, reply.indexOf("\"", indexOfValue + 10));
                        String signature = reply.substring(indexOfSignature + 14, reply.indexOf("\"", indexOfSignature + 14));
                        ((EntityPlayer) npc).getProfile().getProperties().put("textures", new Property("textures", skin, signature));
                        show();
                    }
                    else {
                        Bukkit.getConsoleSender().sendMessage("Connection could not be opened when fetching player skin (Response code " + connection.getResponseCode() + ", " + connection.getResponseMessage() + ")");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Skywars.getInstance());
    }
    public void show() {
        for (Player player : npc.getWorld().getWorld().getPlayers()) {
            show(player);
        }
    }
    public void show(Player player) {
        if (!player.getWorld().getName().equals(npc.getWorld().getWorld().getName())) return;
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        if (type == NPCType.PLAYER && npc instanceof EntityPlayer) {

            EntityPlayer entityPlayer = (EntityPlayer) npc;
            connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer));
            connection.sendPacket(new PacketPlayOutNamedEntitySpawn(entityPlayer));
            connection.sendPacket(new PacketPlayOutEntityHeadRotation(npc, (byte) (npc.yaw * 256 / 360)));

            hideFromTablist(player);
        } else {
            connection.sendPacket(new PacketPlayOutSpawnEntityLiving((EntityLiving) npc));
        }
    }
    public void hide() {
        for (Player player : Bukkit.getOnlinePlayers()) hide(player);
    }
    public void hide(Player player) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(new PacketPlayOutEntityDestroy(npc.getId()));
    }
    public void setItemInHand(ItemStack item) {
        npc.setEquipment(0, item);
    }
    public void setEquipment(int[] slots, ItemStack[] equipments) {
        for (int i = 0; i < Math.min(slots.length, equipments.length); i++) {
            npc.setEquipment(i, equipments[i]);
        }
    }
    @EventHandler
    public void entityDamage(EntityDamageEvent event) {
        if (isEqual(event.getEntity())) {
            event.setDamage(0);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void playerInteract(PlayerInteractEntityEvent event) {
        if (!isEqual(event.getRightClicked())) return;
        event.setCancelled(true);
        if (clickEvent != null) clickEvent.accept(event.getPlayer());
    }
    @EventHandler
    public void entityDamage(EntityDamageByEntityEvent event) {
        if (isEqual(event.getEntity()))  event.setCancelled(true);
        if (clickEvent != null && event.getDamager() != null && event.getDamager() instanceof Player)
            clickEvent.accept((Player) event.getEntity());
    }

    private boolean isEqual(org.bukkit.entity.Entity entity) {
        return ((CraftEntity) entity).getHandle() == npc;
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
        npc.world.removeEntity(npc);
        if (npc instanceof EntityPlayer) {
            PacketPlayOutPlayerInfo packet =
                    new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, (EntityPlayer) npc);
            for (Player p : Bukkit.getOnlinePlayers()) {
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
            }
        }
    }
}
