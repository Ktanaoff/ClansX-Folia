package cla.Entt.ClansX.config;

import cla.Entt.ClansX.ClansX;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class MainConfigManager {

    private CustomConfig configFile;
    private CustomConfig dataFile;
    private ClansX plugin;
    private Boolean welcomeMessageEnabled;
    private List<String> welcomeMessageMessage;
    private List<String> namesBlocked;
    private Boolean namesBlockedEnabled;

    public MainConfigManager(ClansX plugin) {
        this.plugin = plugin;
        configFile = new CustomConfig("config.yml", null, plugin);
        dataFile = new CustomConfig("data.yml", null, plugin);
        dataFile.registerData();
        configFile.registerConfig();

        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = configFile.getConfig();
        FileConfiguration data = dataFile.getData();
        welcomeMessageEnabled = config.getBoolean("config.welcome-message.enabled", false);
        welcomeMessageMessage = config.getStringList("config.welcome-message.message");
        namesBlockedEnabled = config.getBoolean("config.names-blocked.enabled");
        namesBlocked = config.getStringList("config.names-blocked.blocked");
    }

    public String getPlayerClan(String playerName) {
        FileConfiguration data = getData();

        for (String clanName : data.getConfigurationSection("Data").getKeys(false)) {
            List<String> users = data.getStringList("Data." + clanName + ".users");

            if (users.contains(playerName)) {
                return clanName;
            }
        }

        return null;
    }

    public void loadData() {
        FileConfiguration data = dataFile.getData();
    }

    public void saveData() {
        dataFile.saveData();
    }

    public void reloadConfig() {
        configFile.reloadConfig();
        loadConfig();
    }

    public FileConfiguration getData() {
        loadData();
        return dataFile.getData();
    }

    public void reloadData() {
        dataFile.reloadData();
        loadData();
    }

    public List<String> getWelcomeMessageMessage() {
        return welcomeMessageMessage;
    }

    public boolean isWelcomeMessageEnabled() {
        return welcomeMessageEnabled;
    }

    public List<String> namesBlocked() {
        return namesBlocked;
    }

    public boolean isNamesBlockedEnabled() {
        return namesBlockedEnabled;
    }
}