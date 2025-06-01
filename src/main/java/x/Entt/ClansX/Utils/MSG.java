package x.Entt.ClansX.Utils;

import net.md_5.bungee.api.ChatColor;

import java.util.List;
import java.util.stream.Collectors;

public class MSG {
   public static String color(String msg) {
      return ChatColor.translateAlternateColorCodes('&', msg);
   }

   public static List<String> colorList(List<String> messages) {
      return messages.stream()
              .map(MSG::color)
              .collect(Collectors.toList());
   }
}