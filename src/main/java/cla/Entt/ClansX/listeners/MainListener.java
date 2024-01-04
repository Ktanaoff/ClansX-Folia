package cla.Entt.ClansX.listeners;

import cla.Entt.ClansX.ClansX;
import cla.Entt.ClansX.config.MainConfigManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import cla.Entt.ClansX.utils.MessageUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainListener implements Listener {
    private ClansX plugin;

    public MainListener(ClansX plugin) {
        this.plugin = plugin;
    }

    private Map<String, String> pendingInvitations = new HashMap<>();

    private void inviteToClan(CommandSender sender, String playerToInvite, String clanName) {
        pendingInvitations.put(playerToInvite, clanName);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        MainConfigManager mainConfigManager = plugin.getMainConfigManager();

        if (mainConfigManager.isWelcomeMessageEnabled()) {
            List<String> message = mainConfigManager.getWelcomeMessageMessage();
            for (String m : message) {
                player.sendMessage(MessageUtils.colorMsg(m.replace("%player%", player.getName())));
            }
        }

        String playerName = player.getName();
        if (pendingInvitations.containsKey(playerName)) {
            String clanInvitedTo = pendingInvitations.get(playerName);
            player.sendMessage(ClansX.prefix + "You have been invited to join clan: " + clanInvitedTo);
        }
    }
}