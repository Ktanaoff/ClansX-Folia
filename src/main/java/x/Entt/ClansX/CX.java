package x.Entt.ClansX;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import x.Entt.ClansX.CMDs.CCMD;
import x.Entt.ClansX.CMDs.ACMD;
import x.Entt.ClansX.Events.Listener;
import x.Entt.ClansX.Utils.*;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Objects;

public class CX extends JavaPlugin {
   public String version = getDescription().getVersion();
   public static Economy econ;
   public static String prefix;
   private FileHandler fh;

    @Override
   public void onEnable() {
      saveDefaultConfig();
      prefix = getConfig().getString("prefix", "&3&L[Clans&b&lX&3&l] ");
      fh = new FileHandler(this);

      if (getConfig().getBoolean("vault-integration.enabled", true) && !setupEconomy()) {
         getLogger().severe("Vault dependency not found, disabling plugin!");
         Bukkit.getPluginManager().disablePlugin(this);
         return;
      }

      setupMetrics();
      registerCommands();
      registerEvents();
      registerFiles();

        Updater updater = new Updater(this, 114316);

        searchUpdates();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
         new PAPI(this).registerPlaceholders();
      }

      logToConsole("&av" + getDescription().getVersion() + " &2Enabled!");
   }

   @Override
   public void onDisable() {
      logToConsole("&av" + getDescription().getVersion() + " &cDisabled");
   }

   private void setupMetrics() {
      new Metrics(this, 20912).addCustomChart(new Metrics.SimplePie("vault_enabled",
              () -> getConfig().getString("vault-integration.enabled", "true")));
   }

   private void registerCommands() {
      Objects.requireNonNull(getCommand("clansx")).setExecutor(new ACMD(this));
      Objects.requireNonNull(getCommand("clans")).setExecutor(new CCMD(this));
   }

   private void registerEvents() {
      getServer().getPluginManager().registerEvents(new Listener(this), this);
   }

   public void registerFiles() {
      fh.saveDefaults();
   }

   public void searchUpdates() {
      String downloadUrl = "https://www.spigotmc.org/resources/clansx-the-best-clan-system-1-8-1-21.114316/";
      TextComponent link = new TextComponent(MSG.color("&e&lClick here to download the update!"));
      link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, downloadUrl));

      boolean updateAvailable = false;
      String latestVersion = "unknown";

      try {
         Updater updater = new Updater(this, 115289);
         updateAvailable = updater.isUpdateAvailable();
         latestVersion = updater.getLatestVersion();
      } catch (Exception e) {
         logToConsole("&cError checking for updates: " + e.getMessage());
      }

      if (updateAvailable) {
         Bukkit.getConsoleSender().sendMessage(MSG.color("&2&l============= " + prefix + "&2&l============="));
         Bukkit.getConsoleSender().sendMessage(MSG.color("&6&lNEW VERSION AVAILABLE!"));
         Bukkit.getConsoleSender().sendMessage(MSG.color("&e&lCurrent Version: &f" + version));
         Bukkit.getConsoleSender().sendMessage(MSG.color("&e&lLatest Version: &f" + latestVersion));
         Bukkit.getConsoleSender().sendMessage(MSG.color("&e&lDownload it here: &f" + downloadUrl));
         Bukkit.getConsoleSender().sendMessage(MSG.color("&2&l============= " + prefix + "&2&l============="));

         for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("cx.admin")) {
               player.sendMessage(MSG.color(prefix + "&e&lA new plugin update is available!"));
               player.spigot().sendMessage(link);
            }
         }
      }
   }

   private boolean setupEconomy() {
      RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
      econ = (provider != null) ? provider.getProvider() : null;

      if (econ == null) {
         logToConsole("&cEconomyProvider is null");
      }
      return econ != null;
   }

   private void logToConsole(String message) {
      Bukkit.getConsoleSender().sendMessage(MSG.color(prefix + message));
   }

   public FileHandler getFH() {
      return fh;
   }
}