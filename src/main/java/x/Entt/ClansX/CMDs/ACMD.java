package x.Entt.ClansX.CMDs;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import x.Entt.ClansX.CX;
import x.Entt.ClansX.Utils.FileHandler;
import x.Entt.ClansX.Utils.MSG;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static x.Entt.ClansX.CX.prefix;

public class ACMD implements CommandExecutor, TabCompleter {
    private final CX plugin;

    public ACMD(CX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            return handleConsoleCommand(sender, args);
        }

        if (!sender.hasPermission("cx.admin")) {
            sender.sendMessage(MSG.color(prefix + "&cYou don't have permissions to use this command"));
            return true;
        }

        if (args.length < 1) {
            help(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reports":
                listReports(sender);
                break;
            case "reload":
                reload(sender);
                break;
            case "ban":
                ban(sender, args);
                break;
            case "unban":
                unban(sender, args);
                break;
            case "clear":
                clear(sender);
                break;
            default:
                help(sender);
                break;
        }
        return true;
    }

    private boolean handleConsoleCommand(CommandSender sender, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            reload(sender);
            return true;
        } else {
            sender.sendMessage(MSG.color(prefix + "&cConsole Commands: &f/clx reload."));
            return true;
        }
    }

    private void help(CommandSender sender) {
        sender.sendMessage(MSG.color(""));
        sender.sendMessage(MSG.color("&3&l=== &3&L[Clans&b&lX&3&l] &3&l==="));
        sender.sendMessage(MSG.color(""));
        sender.sendMessage(MSG.color("&e&lREPORTS: &2List all clans with reports and their reasons."));
        sender.sendMessage(MSG.color("&2&lRELOAD: &2Reload the plugin."));
        sender.sendMessage(MSG.color("&5&lUNBAN: &cUnban a clan."));
        sender.sendMessage(MSG.color("&c&lBAN: &cBan a clan (no duration specified for perma ban)."));
        sender.sendMessage(MSG.color("&4&lCLEAR: &cDelete the entire Data.yml &c(⚠ WARNING ⚠)."));
        sender.sendMessage(MSG.color(""));
        sender.sendMessage(MSG.color("&3&l=== &3&L[Clans&b&lX&3&l] &3&l==="));
        sender.sendMessage(MSG.color(""));
    }

    private void listReports(CommandSender sender) {
        FileHandler fh = plugin.getFH();
        FileConfiguration data = fh.getData();

        if (!data.contains("Clans")) {
            sender.sendMessage(MSG.color(prefix + "&cThere are no clans to report."));
            return;
        }

        Map<String, List<String>> clansWithReports = new HashMap<>();
        for (String clan : Objects.requireNonNull(data.getConfigurationSection("Clans")).getKeys(false)) {
            List<String> reports = data.getStringList("Clans." + clan + ".Reports");
            if (!reports.isEmpty()) {
                clansWithReports.put(clan, reports);
            }
        }

        if (clansWithReports.isEmpty()) {
            sender.sendMessage(MSG.color(prefix + "&cNo clans have reports."));
            return;
        }

        sender.sendMessage(MSG.color("&2==== REPORT LIST: ===="));
        for (Map.Entry<String, List<String>> entry : clansWithReports.entrySet()) {
            String clan = entry.getKey();
            List<String> reports = entry.getValue();
            sender.sendMessage(MSG.color("&2" + clan + " &f:"));
            reports.forEach(report -> sender.sendMessage(MSG.color("  &7- &f" + report)));
        }
        sender.sendMessage(MSG.color("&2======================"));
    }

    private void clear(CommandSender sender) {
        FileHandler fh = plugin.getFH();
        FileConfiguration data = fh.getData();

        data.set("Clans", null);
        fh.saveData();

        sender.sendMessage(MSG.color(prefix + "&2Reformatted plugin (config not changed)."));
    }

    private void reload(CommandSender sender) {
        FileHandler fh = plugin.getFH();
        fh.reloadConfig();
        fh.reloadData();
        sender.sendMessage(MSG.color(prefix + "&2Plugin and all files reloaded!"));
    }

    private void ban(CommandSender sender, String[] args) {
        if (args.length < 2 || args.length > 3) {
            sender.sendMessage(MSG.color(prefix + "&c&lUSE: &f/clx ban <clanName> [duration]"));
            sender.sendMessage(MSG.color(prefix + "&c&lIf you don't put in the time the clan will be eliminated"));
            return;
        }

        String clanName = args[1];
        String reason = args.length == 3 ? args[2] : "Banned by admin";

        FileHandler fh = plugin.getFH();
        FileConfiguration dataConfig = fh.getData();

        if (!dataConfig.contains("Clans." + clanName)) {
            sender.sendMessage(MSG.color(prefix + "&cClan '" + clanName + "' does not exist."));
            return;
        }

        List<String> members = dataConfig.getStringList("Clans." + clanName + ".Users");
        for (String member : members) {
            Player player = Bukkit.getPlayer(member);
            if (player != null) {
                player.ban(reason, (Date) null, "Banned by admin", true);
                player.kickPlayer(MSG.color(prefix + "&cYou have been banned from your clan."));
            }
        }

        sender.sendMessage(MSG.color(prefix + "&cClan '" + clanName + "' has been permanently banned."));
    }

    private void unban(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(MSG.color(prefix + "&cUsage: /clx unban <clanName>"));
            return;
        }

        String clanName = args[1];
        FileHandler fh = plugin.getFH();
        FileConfiguration dataConfig = fh.getData();

        if (!dataConfig.contains("Clans." + clanName)) {
            sender.sendMessage(MSG.color(prefix + "&cClan '" + clanName + "' does not exist."));
            return;
        }

        List<String> members = dataConfig.getStringList("Clans." + clanName + ".Users");
        for (String member : members) {
            Objects.requireNonNull(Bukkit.getPlayerExact(member)).ban(null, (Date) null, null, false);
        }

        sender.sendMessage(MSG.color(prefix + "&2Clan '" + clanName + "' has been unbanned."));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player) || !sender.hasPermission("cx.admin")) {
            return args.length == 1 ? Collections.singletonList("reload") : Collections.emptyList();
        }

        if (args.length == 1) {
            return Stream.of("reload", "ban", "unban", "clear", "reports")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && Arrays.asList("ban", "unban").contains(args[0].toLowerCase())) {
            return getClanNames().stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && "ban".equalsIgnoreCase(args[0])) {
            return Stream.of("1d", "1h", "1m", "1w")
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    private List<String> getClanNames() {
        FileHandler fh = plugin.getFH();
        FileConfiguration dataConfig = fh.getData();
        if (dataConfig.contains("Clans")) {
            return new ArrayList<>(Objects.requireNonNull(dataConfig.getConfigurationSection("Clans")).getKeys(false));
        }
        return Collections.emptyList();
    }
}