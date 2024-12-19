package x.Entt.ClansX.Utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import x.Entt.ClansX.CX;
import java.util.List;

public class PAPI extends PlaceholderExpansion {

    private final CX plugin;
    private final FileConfiguration dataConfig;

    public PAPI(CX plugin) {
        this.plugin = plugin;
        this.dataConfig = plugin.getFH().getData();
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "cx";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) return null;
        String clanName = getPlayerClan(player.getName());

        switch (identifier.toLowerCase()) {
            case "prefix":
                return CX.prefix;
            case "clan_leader":
                return (clanName != null) ? dataConfig.getString("Clans." + clanName + ".Leader", "N/A") : "No Clan";
            case "clan_membercount":
                return (clanName != null) ? String.valueOf(dataConfig.getStringList("Clans." + clanName + ".Users").size()) : "No Clan";
            case "clan_founder":
                return (clanName != null) ? dataConfig.getString("Clans." + clanName + ".Founder", "N/A") : "No Clan";
            case "clan_name":
                return (clanName != null) ? clanName : "No Clan";
            default:
                return null;
        }
    }

    private String getPlayerClan(String playerName) {
        ConfigurationSection clansSection = dataConfig.getConfigurationSection("Clans");
        if (clansSection != null) {
            for (String clan : clansSection.getKeys(false)) {
                List<String> users = dataConfig.getStringList("Clans." + clan + ".Users");
                if (users.contains(playerName)) {
                    return clan;
                }
            }
        }
        return null;
    }

    public void registerPlaceholders() {
        if (!this.register()) {
            plugin.getLogger().warning("Can't register the placeholders");
        } else {
            plugin.getLogger().info("ClansX placeholders registered!");
        }
    }
}