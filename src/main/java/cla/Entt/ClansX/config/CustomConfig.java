package cla.Entt.ClansX.config;

import cla.Entt.ClansX.ClansX;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class CustomConfig {
    private ClansX plugin;
    private String fileName;
    private FileConfiguration fileConfiguration = null;
    private File file = null;
    private String folderName;

    public CustomConfig(String fileName, String folderName, ClansX plugin) {
        this.fileName = fileName;
        this.folderName = folderName;
        this.plugin = plugin;
    }

    public void registerConfig() {
        file = new File(getFilePath());

        if (!file.exists()) {
            plugin.saveResource(getResourcePath(), false);
        }

        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    public void saveConfig() {
        try {
            fileConfiguration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        if (fileConfiguration == null) {
            reloadConfig();
        }
        return fileConfiguration;
    }

    public boolean reloadConfig() {
        file = new File(getFilePath());
        fileConfiguration = YamlConfiguration.loadConfiguration(file);

        if (file != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(file);
            fileConfiguration.setDefaults(defConfig);
        }
        return true;
    }

    public void registerData() {
        registerConfig();
    }

    public void saveData() {
        try {
            fileConfiguration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getData() {
        if (fileConfiguration == null) {
            reloadData();
        }
        return fileConfiguration;
    }

    public boolean reloadData() {
        file = new File(getFilePath());
        fileConfiguration = YamlConfiguration.loadConfiguration(file);

        if (file != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(file);
            fileConfiguration.setDefaults(defConfig);
        }
        return true;
    }

    private String getResourcePath() {
        if (folderName != null) {
            return folderName + File.separator + fileName;
        } else {
            return fileName;
        }
    }

    private String getFilePath() {
        if (folderName != null) {
            return plugin.getDataFolder() + File.separator + folderName + File.separator + fileName;
        } else {
            return plugin.getDataFolder() + File.separator + fileName;
        }
    }
}