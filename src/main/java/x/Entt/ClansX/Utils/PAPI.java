package x.Entt.ClansX.Utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import x.Entt.ClansX.CX;

import java.util.Objects;

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
        String clanName = getClanOfPlayer(player);
        if (clanName == null) {
            return "";
        }

        switch (identifier) {
            case "prefix":
                return CX.prefix;

            case "clan.leader":
                return dataConfig.getString("Clans." + clanName + ".Leader", "N/A");

            case "clan.membercount":
                return String.valueOf(dataConfig.getStringList("Clans." + clanName + ".Users").size());

            case "clan.founder":
                return dataConfig.getString("Clans." + clanName + ".Founder", "N/A");

            case "clan.name":
                return clanName;

            default:
                return null;
        }
    }

    private String getClanOfPlayer(Player player) {
        for (String clan : Objects.requireNonNull(dataConfig.getConfigurationSection("Clans")).getKeys(false)) {
            if (dataConfig.getStringList("Clans." + clan + ".Users").contains(player.getName())) {
                return clan;
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