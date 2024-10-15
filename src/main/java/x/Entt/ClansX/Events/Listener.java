package x.Entt.ClansX.Events;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import x.Entt.ClansX.CX;
import x.Entt.ClansX.Utils.FileHandler;
import x.Entt.ClansX.Utils.MSG;
import x.Entt.ClansX.Utils.Updater;
import static x.Entt.ClansX.CX.prefix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class Listener implements org.bukkit.event.Listener {
   private final CX plugin;
   private final Map<String, String> pendingInvitations = new HashMap<>();

   public Listener(CX plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent event) {
       String downloadUrl = "https://www.spigotmc.org/resources/clansx-the-best-clan-system-1-8-1-21.114316/";
       TextComponent link = new TextComponent(MSG.color(downloadUrl));
       link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, downloadUrl));
       Player player = event.getPlayer();
       FileHandler fh = plugin.getFH();

       if (fh.getConfig().getBoolean("welcome-message.enabled")) {
           List<String> messages = fh.getConfig().getStringList("welcome-message.message");

           for (String message : messages) {
               player.sendMessage(MSG.color(player, message));
           }
       }

       String playerName = player.getName();
       if (this.pendingInvitations.containsKey(playerName)) {
           String clanInvitedTo = this.pendingInvitations.get(playerName);
           player.sendMessage(MSG.color(prefix + "You have been invited to join clan: " + clanInvitedTo));
       }

       if (player.hasPermission("cx.admin") && Updater.isUpdateAvailable()) {
           player.sendMessage(MSG.color(player, prefix + "&2&lHey %player%, there is a new version of the plugin"));
           player.sendMessage(MSG.color(player, "&eDownload the new version here: " + link));
       }
   }

   @EventHandler
   public void onKill(PlayerDeathEvent event) {
      FileHandler fh = plugin.getFH();
      int killReward = fh.getConfig().getInt("vault-integration.earn.kill-enemy");
      if (fh.getConfig().getBoolean("vault-integration.enabled")) {
          event.getEntity();
          Player victim = event.getEntity();
          Player killer = victim.getKiller();
          if (killer != null) {
              CX.econ.depositPlayer(killer, killReward);
              killer.sendMessage(MSG.color(prefix + "&2You Won: &e&l" + killReward));
          }
      }
   }
}