package x.Entt.ClansX.Utils;

import me.clip.placeholderapi.PlaceholderAPI;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import x.Entt.ClansX.CX;

public class MSG {

   public static String color(String message) {
      message = message.replace("%prefix%", CX.prefix);

      return ChatColor.translateAlternateColorCodes('&', message);
   }

   public static String color(Player player, String message) {
      message = message.replace("%prefix%", CX.prefix);

      if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
         message = PlaceholderAPI.setPlaceholders(player, message);
      }

      return ChatColor.translateAlternateColorCodes('&', message);
   }
}