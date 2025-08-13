package com.HauBaka.modules.menu;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketDataSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutCustomPayload;
import net.minecraft.server.v1_8_R3.PlayerConnection;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftMetaBook;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

public class BookMenu {
    private final ItemStack book;
    @Getter
    private ItemStack returnItem;
    public BookMenu(List<TextComponent> text_pages) {
        book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Special book!");
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        List<IChatBaseComponent> pages;

        try {
            pages = (List<IChatBaseComponent>) CraftMetaBook.class.getDeclaredField("pages").get(meta);
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
            return;
        }


        for (TextComponent page : text_pages) {
            pages.add(IChatBaseComponent.ChatSerializer.a(ComponentSerializer.toString(page)));
        }
        meta.setAuthor("HauBaka");
        book.setItemMeta(meta);
    }
    public boolean open(Player player) {
        returnItem = player.getItemInHand();
        player.setItemInHand(book);
        /*Sending open book packet */
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

        ByteBuf buffer = Unpooled.buffer(256);
        buffer.setByte(0, (byte) 0);
        buffer.writerIndex(1);
        PacketPlayOutCustomPayload packetPlayOutCustomPayload = new PacketPlayOutCustomPayload("MC|BOpen", new PacketDataSerializer(buffer));
        playerConnection.sendPacket(packetPlayOutCustomPayload);
        player.setItemInHand(returnItem);
        return false;
    }
}