package com.HauBaka.utils;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Utils {
    private static final String[] letters = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "X", "W", "Y", "Z", "0", "1",  "2", "3", "4", "5", "6", "7", "8", "9", "0"};

    /**
     *
     * @param a
     * @param b
     * @return any number in [a,b]
     */
    public static  int randomInRange(int a, int b) {
        if (a>b) return randomInRange(b, a);
        Random rand = new Random();
        return rand.nextInt(b - a + 1) + a;
    }
    public static boolean chanceOf(int n) {
        return randomInRange(0,100) < n;
    }

    /**
     *
     * @param n
     * @return string which parsed in format like 12345 -> 12,345
     */
    public static String parseInt(int n) {
        if (n==0) return "0";
        StringBuilder result = new StringBuilder();
        int cnt =0;
        while (n >0) {
            if (cnt == 3) {
                result.insert(0, ",");
                cnt=0;
            }
            result.insert(0, (n % 10));
            n/=10;
            cnt++;
        }
        return result.toString();
    }
    public static String secondsToTime(int seconds) {
        int minutes = seconds/60;
        int second = seconds%60;
        return minutes + ":" + (second < 10 ? "0":"") + second;
    }
    /**
     *
     * @param format: Default(dd/MM/yyyy)
     * @return 18/08/2006 for example.
     */
    public static String getTodayFormat(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }
    public static String getTodayFormat() {
        return getTodayFormat("dd/MM/yyyy");
    }
    /**
     * @param n : 2.3456
     * @param format : 0.00
     * @return 2.35
     */
    public static String toDecimalFormat(double n, String format) {
        DecimalFormat df = new DecimalFormat(format);
        return df.format(n);
    }

    /**
     *
     * @param s : input string
     * @return parsed string to rainbow string in minecraft color
     */
    public static String toRainbow(String s) {
        StringBuilder result = new StringBuilder();
        String[] colors = new String[]{"§4", "§c", "§6", "§e", "§a", "§b", "§3", "§9", "§d", "§1", "§3", "§b", "§a", "§e", "§6", "§c"};
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == ' ') result.append(' ');
            else result.append(colors[i % colors.length]).append(s.charAt(i));
        }
        return result.toString();
    }

    /**
     *
     * @param name
     * @return parsed name by removing '_', and trimming
     */
    public static String toBetterName(String name) {
        if (name.isEmpty()) return name;
        name = name.toLowerCase();
        String[] words = name.split("_");
        StringBuilder result= new StringBuilder();
        for (String word : words) {
            if (result.length() == 0) result = new StringBuilder(word.substring(0, 1).toUpperCase() + word.substring(1));
            else result.append(" ").append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
        }

        return result.toString().trim();
    }

    public static  String generateID(int size) {
        StringBuilder ID = new StringBuilder();
        for (int i = 1; i <= size; i++) {
            ID.append(letters[Utils.randomInRange(0, letters.length - 1)]);
        }
        return ID.toString();
    }

    public static ItemStack buildItem(Material material, String name, List<String> lore, Map<Enchantment, Integer> enchantments) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (name != null)
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        if (lore != null && !lore.isEmpty()) {
            List<String> coloredLore = new ArrayList<>();
            for (String s : lore) {
                coloredLore.add(ChatColor.translateAlternateColorCodes('&', s));
            }
            meta.setLore(coloredLore);
        }

        if (enchantments != null) {
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                meta.addEnchant(entry.getKey(), entry.getValue(), true);
            }
        }

        item.setItemMeta(meta);
        return item;
    }
    public static ItemStack buildItem(ItemStack itemStack, String name, List<String> lore, Map<Enchantment, Integer> enchantments) {
        ItemStack item = itemStack.clone();
        ItemMeta meta = item.getItemMeta();

        if (name != null)meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        if (lore != null && !lore.isEmpty()) {
            List<String> coloredLore = new ArrayList<>();
            for (String s : lore) {
                coloredLore.add(ChatColor.translateAlternateColorCodes('&', s));
            }
            meta.setLore(coloredLore);
        }

        if (enchantments != null) {
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                meta.addEnchant(entry.getKey(), entry.getValue(), true);
            }
        }

        item.setItemMeta(meta);
        return item;
    }
    public static Location getTargetBlockLocation(Player player) {
        Location loc = player.getTargetBlock((Set<Material>) null, 100).getLocation();
        if (loc.getBlock().getType().equals(Material.AIR)) {
            return null;
        }
        while (loc.getBlock().getType() != Material.AIR) {
            loc.add(0,1,0);
        }

        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();

        loc = loc.getBlock().getLocation();
        loc.add(0.5,0,0.5);
        loc.setYaw(yaw);
        loc.setPitch(pitch);

        return loc;
    }
    public static float getAbsoluteYaw(double yaw) {
        yaw = (yaw % 360 + 360) % 360;
        int quadrant = (int) Math.round(yaw / 90.0) % 4;

        return quadrant * 90f;
    }

}
