package com.HauBaka.object;

import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArmorStand;
import org.bukkit.entity.ArmorStand;

import java.util.ArrayList;
import java.util.List;

public class Hologram {
    private final Location baseLocation;
    private final List<ArmorStand> armorStands = new ArrayList<>();

    public Hologram(Location location) {
        if (location == null) throw new NullPointerException("Location cannot be null");
        this.baseLocation = location.clone().add(0.5, 0, 0.5);
    }

    public void setText(String text) {
        setLines(text);
    }

    public void setLines(String... lines) {
        clearLines();
        double yOffset = 0.25*(lines.length+1);
        for (String line : lines) {
            yOffset -= 0.25;
            if (line.isEmpty()) continue;
            spawnLine(baseLocation.clone().add(0, yOffset, 0), ChatColor.translateAlternateColorCodes('&', line));
        }
    }

    public void addLine(String text) {
        double yOffset = -armorStands.size() * 0.25;
        if (text.isEmpty()) text = ChatColor.RESET.toString();
        spawnLine(baseLocation.clone().add(0, yOffset, 0), ChatColor.translateAlternateColorCodes('&', text));
    }
    public void setLine(int index, String text) {
        if (index < 0 || index >= armorStands.size()) {
            throw new IndexOutOfBoundsException("Invalid line index: " + index);
        }

        ArmorStand as = armorStands.get(index);
        if (as != null && !as.isDead()) {
            as.setCustomName(ChatColor.translateAlternateColorCodes('&', text));
        }
    }

    public void clearLines() {
        destroy();
        armorStands.clear();
    }

    private void spawnLine(Location loc, String text) {
        ArmorStand as = loc.getWorld().spawn(loc, ArmorStand.class);
        as.setVisible(false);
        as.setGravity(false);
        as.setCustomNameVisible(true);
        as.setMarker(true);
        as.setCustomName(text);

        // NMS: set Invulnerable
        EntityArmorStand nmsAS = ((CraftArmorStand) as).getHandle();
        NBTTagCompound tag = new NBTTagCompound();
        nmsAS.c(tag);
        tag.setBoolean("Invulnerable", true);
        nmsAS.f(tag);

        armorStands.add(as);
    }

    public void destroy() {
        for (ArmorStand as : armorStands) {
            if (as != null && !as.isDead()) {
                as.remove();
            }
        }
    }
}
