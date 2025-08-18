package com.HauBaka.object.cage;

import com.HauBaka.enums.ObjectRarity;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.List;

public class Cage {
    public static class CageBlock {
        private final int x;
        private final int y;
        private final int z;
        private final byte data;
        private final Material material;
        public CageBlock(int x, int y, int z, Material material, byte data) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.material = material;
            this.data = data;
        }
        public void place(Location base) {
            Block block = base.clone().add(x, y, z).getBlock();
            block.setType(material);
            block.setData(data, false);
        }

        public void remove(Location base) {
            Block block = base.clone().add(x, y, z).getBlock();
            block.setType(Material.AIR);
        }
    }
    private final List<CageBlock> blocks;
    @Getter
    ObjectRarity rarity;
    @Getter
    String name;
    public Cage(String name, List<CageBlock> blocks, ObjectRarity rarity) {
        this.blocks = blocks;
        this.name = name;
        this.rarity = rarity;
    }
    public void place(Location baseLocation) {
        for (CageBlock block : blocks)
            block.place(baseLocation);
    }
    public void remove(Location baseLocation) {
        for (CageBlock block : blocks)
            block.remove(baseLocation);
    }

}
