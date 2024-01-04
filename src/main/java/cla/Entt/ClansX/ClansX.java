package cla.Entt.ClansX;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import cla.Entt.ClansX.commands.MainCmd;
import cla.Entt.ClansX.utils.MessageUtils;
import cla.Entt.ClansX.listeners.MainListener;
import cla.Entt.ClansX.commands.ClanCmd;
import cla.Entt.ClansX.config.MainConfigManager;

public class ClansX extends JavaPlugin {

    public static String prefix = "&9[&1&lClans&b&lX&9] ";

    private String version = getDescription().getVersion();
    private MainConfigManager mainConfigManager;

    public static ClansX instance;

    public void onEnable() {
        saveDefaultConfig();
        MetricsLite metrics = new MetricsLite(this);
        registerCommands();
        registerEvents();

        try {
            mainConfigManager = new MainConfigManager(this);
            Bukkit.getConsoleSender().sendMessage(MessageUtils.colorMsg("&c<-------------------->"));
            Bukkit.getConsoleSender().sendMessage(MessageUtils.colorMsg("&ePlugin " + prefix + " &2v" + version));
            Bukkit.getConsoleSender().sendMessage(MessageUtils.colorMsg("&ehas been &2enabled!"));
            Bukkit.getConsoleSender().sendMessage(MessageUtils.colorMsg("&c<-------------------->"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(MessageUtils.colorMsg("&c<-------------------->"));
        Bukkit.getConsoleSender().sendMessage(MessageUtils.colorMsg("&ePlugin&2&m " + prefix + "&2v" + version));
        Bukkit.getConsoleSender().sendMessage(MessageUtils.colorMsg("&ehas been &cdisabled!"));
        Bukkit.getConsoleSender().sendMessage(MessageUtils.colorMsg("&c<-------------------->"));
    }

    public void registerCommands() {
        this.getCommand("clansx").setExecutor(new MainCmd(this));
        this.getCommand("clans").setExecutor(new ClanCmd(this));
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(new MainListener(this), this);
    }

    public MainConfigManager getMainConfigManager() {
        return mainConfigManager;
    }
}