package x.Entt.ClansX.Events;

import x.Entt.ClansX.CX;
import x.Entt.ClansX.Utils.FileHandler;
import x.Entt.ClansX.Utils.MSG;

import me.clip.placeholderapi.PlaceholderAPI;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static x.Entt.ClansX.CX.prefix;

public class Listener implements org.bukkit.event.Listener {
    private final CX plugin;
    private final Map<String, String> pendingInvitations = new HashMap<>();
    private final boolean placeholderAPIEnabled;

    public Listener(CX plugin) {
        this.plugin = plugin;
        this.placeholderAPIEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FileHandler fh = plugin.getFH();

        if (fh.getConfig().getBoolean("welcome-message.enabled")) {
            List<String> messages = fh.getConfig().getStringList("welcome-message.message");

            for (String message : messages) {
                String finalMessage = placeholderAPIEnabled
                        ? PlaceholderAPI.setPlaceholders(player, message)
                        : message;
                player.sendMessage(MSG.color(finalMessage));
            }
        }

        String playerName = player.getName();
        if (this.pendingInvitations.containsKey(playerName)) {
            String clanInvitedTo = this.pendingInvitations.get(playerName);
            String invitationMessage = prefix + "You have been invited to join clan: " + clanInvitedTo;

            String finalMessage = placeholderAPIEnabled
                    ? PlaceholderAPI.setPlaceholders(player, invitationMessage)
                    : invitationMessage;
            player.sendMessage(MSG.color(finalMessage));
        }
    }

    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        FileHandler fh = plugin.getFH();
        int killReward = fh.getConfig().getInt("vault-integration.earn.kill-enemy");

        if (fh.getConfig().getBoolean("vault-integration.enabled")) {
            Player victim = event.getEntity();
            Player killer = victim.getKiller();

            if (killer != null) {
                CX.econ.depositPlayer(killer, killReward);

                String rewardMessage = prefix + "&2You Won: &e&l" + killReward;

                String finalMessage = placeholderAPIEnabled
                        ? PlaceholderAPI.setPlaceholders(killer, rewardMessage)
                        : rewardMessage;
                killer.sendMessage(MSG.color(finalMessage));
            }
        }
    }
}