package com.HauBaka.utils;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatUtils {

    public static void sendComplexMessage(CommandSender target, TextComponent... parts) {
        if (!(target instanceof Player)) {
            // Nếu console: in plain text
            StringBuilder sb = new StringBuilder();
            for (TextComponent part : parts) {
                sb.append(part.toLegacyText());
            }
            target.sendMessage(sb.toString());
            return;
        }
        ((Player) target).spigot().sendMessage(parts);
    }

    public static TextComponent simple(String text) {
        text = ChatColor.translateAlternateColorCodes('&', text);
        return new TextComponent(TextComponent.fromLegacyText(text));
    }

    public static TextComponent hover(String text, String hoverText) {
        text = ChatColor.translateAlternateColorCodes('&', text);
        hoverText = ChatColor.translateAlternateColorCodes('&', hoverText);
        TextComponent comp = simple(text);
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(hoverText).create()));
        return comp;
    }

    public static TextComponent link(String text, String url, String hoverText) {
        text = ChatColor.translateAlternateColorCodes('&', text);

        TextComponent comp = simple(text);
        comp.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        if (hoverText != null) {
            hoverText = ChatColor.translateAlternateColorCodes('&', hoverText);
            comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(hoverText).create()));
        }
        return comp;
    }
    public static TextComponent command(String text, String command, String hoverText) {
        text = ChatColor.translateAlternateColorCodes('&', text);

        TextComponent comp = simple(text);
        comp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));

        if (hoverText != null) {
            hoverText = ChatColor.translateAlternateColorCodes('&', hoverText);
            comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(hoverText).create()));
        }

        return comp;
    }
}
