package com.HauBaka.npc;

import lombok.Getter;
import net.minecraft.server.v1_8_R3.*;

@Getter
public enum NPCType {
    ZOMBIE(EntityZombie.class, "ZOMBIE"),
    ARMORSTAND(EntityArmorStand.class, "ARMOR_STAND"),
    SKELETON(EntitySkeleton.class, "SKELETON"),
    WITHER_SKELETON(EntitySkeleton.class, "WITHER_SKELETON"),
    PLAYER(EntityPlayer.class, "PLAYER"),
    CREEPER(EntityCreeper.class, "CREEPER"),
    IRON_GOLEM(EntityIronGolem.class, "IRON_GOLEM"),
    SNOW_GOLEM(EntitySnowman.class, "SNOW_GOLEM"),
    SPIDER(EntitySpider.class, "SPIDER"),
    CAVE_SPIDER(EntityCaveSpider.class, "CAVE_SPIDER"),
    ENDERMAN(EntityEnderman.class, "ENDERMAN"),
    BLAZE(EntityBlaze.class, "BLAZE"),
    WITCH(EntityWitch.class, "WITCH"),
    SILVERFISH(EntitySilverfish.class, "SILVERFISH"),
    SLIME(EntitySlime.class, "SLIME"),
    MAGMA_CUBE(EntityMagmaCube.class, "MAGMA_CUBE"),
    GHAST(EntityGhast.class, "GHAST"),
    WITHER(EntityWither.class, "WITHER"),
    BAT(EntityBat.class, "BAT"),
    OCELOT(EntityOcelot.class, "OCELOT"),
    WOLF(EntityWolf.class, "WOLF"),
    PIG(EntityPig.class, "PIG"),
    SHEEP(EntitySheep.class, "SHEEP"),
    COW(EntityCow.class, "COW"),
    CHICKEN(EntityChicken.class, "CHICKEN"),
    HORSE(EntityHorse.class, "HORSE"),
    RABBIT(EntityRabbit.class, "RABBIT"),
    MOOSHROOM(EntityMushroomCow.class, "MOOSHROOM"),
    VILLAGER(EntityVillager.class, "VILLAGER"),
    ENDER_DRAGON(EntityEnderDragon.class, "ENDER_DRAGON"),
    GIANT(EntityGiantZombie.class, "GIANT"),
    GUARDIAN(EntityGuardian.class, "GUARDIAN"),
    SQUID(EntitySquid.class, "SQUID"),
    ;

    private final Class<? extends Entity> clazz;
    private final String name;

    NPCType(Class<? extends Entity> clazz, String name) {
        this.clazz = clazz;
        this.name = name;
    }
}