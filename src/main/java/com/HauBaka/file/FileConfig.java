package com.HauBaka.file;

import com.HauBaka.Skywars;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class FileConfig {
    private final Skywars skywars;
    private final String filename;
    @Getter
    private File file;
    private FileConfiguration fileConfiguration;

    public FileConfig(String filename) {
        skywars = Skywars.getInstance();
        this.filename = filename;
        file = skywars.getDataFolder();
        if (file == null) throw new NullPointerException();
        this.file = new File(file.toString() + File.separatorChar + this.filename);
    }
    /**
     * Reloads the configuration file from disk and applies defaults from the plugin's resources.
     */
    public void reloadConfig() {
        try {
            this.fileConfiguration = (FileConfiguration) YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(this.file), "UTF-8"));
        } catch (UnsupportedEncodingException | FileNotFoundException cause) {
            cause.printStackTrace();
        }
        InputStream inputStream = this.skywars.getResource(this.filename);
        if (inputStream != null) {
            YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(inputStream);
            this.fileConfiguration.setDefaults((Configuration)yamlConfiguration);
        }
    }
    /**
     * Gets the current loaded configuration.
     */
    public FileConfiguration getConfig() {
        if (this.fileConfiguration == null)
            reloadConfig();
        return this.fileConfiguration;
    }
    /**
     * Saves the current configuration to disk.
     */
    public void saveConfig() {
        if (this.fileConfiguration == null)
            return;
        try {
            getConfig().save(this.file);
        } catch (IOException iOException) {
            Skywars.getLogger().warn("Could not save config to {}", this.fileConfiguration, iOException);
        }
    }
    /**
     * Saves the default configuration file from the plugin's resources to the data folder,
     * if it does not already exist.
     * <p>
     * Use this to ensure a config file is created the first time your plugin runs.
     * </p>
     */
    public void saveDefaultConfig() {
        if (!this.file.exists())
            this.skywars.saveResource(this.filename, false);
    }
    /**
     * Adds a default value to the configuration if the key does not already exist.
     * <p>
     * Example:
     * <pre>
     * addDefault("max-players", 12);
     * </pre>
     * </p>
     *
     * @param paramString The configuration key.
     * @param paramObject The default value to set.
     */
    public void addDefault(String paramString, Object paramObject) {
        if (!this.fileConfiguration.contains(paramString))
            this.fileConfiguration.set(paramString, paramObject);
    }
    /**
     * Updates the configuration by adding any missing keys from the default resource file.
     * <p>
     * This will not overwrite existing keys. Missing keys will be logged and saved.
     * Useful when updating your plugin to add new config options without deleting user changes.
     * </p>
     */
    public void updateConfig() {
        try {
            if (this.file.exists()) {
                HashMap<Object, Object> hashMap = new HashMap<>();
                boolean bool = false;
                InputStream inputStream = skywars.getResource(this.file.getName());
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                new YamlConfiguration();
                YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(inputStreamReader);
                for (String str : yamlConfiguration.getKeys(true)) {
                    if (!getConfig().getKeys(true).contains(str)) {
                        getConfig().set(str, yamlConfiguration.get(str));
                        bool = true;
                        hashMap.put(str, yamlConfiguration.get(str));
                    }
                }
                if (bool) {
                    saveConfig();
                    Skywars.getLogger().info(ChatColor.GOLD + this.filename + ": " + ChatColor.GREEN + "updating a config...");
                    for (Map.Entry<Object, Object> entry : hashMap.entrySet())
                        Skywars.getLogger().info(ChatColor.GOLD + this.filename + ": " + ChatColor.YELLOW + (String)entry.getKey() + " " + ChatColor.GREEN + " value " + ChatColor.YELLOW + entry.getValue());
                    Skywars.getLogger().info(ChatColor.GOLD + this.filename + ": " + ChatColor.GREEN + "config has been updated!");
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public boolean removeFile() {
        return file == null || file.delete();
    }
}
