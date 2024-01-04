package cla.Entt.ClansX.commands;

import cla.Entt.ClansX.ClansX;
import cla.Entt.ClansX.utils.MessageUtils;
import cla.Entt.ClansX.config.MainConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public class MainCmd implements CommandExecutor {
    private ClansX plugin;

    public MainCmd(ClansX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        //console
        if (!(sender instanceof Player)) {
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    reload(sender);
                    return true;
                }
            }
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cConsole Commands: &f/clansx reload."));
            return true;
        }
        if (!(sender.hasPermission("clansx.admin"))) {
            sender.sendMessage
                    (MessageUtils.colorMsg(ClansX.prefix + "&cYou don't have permissions to use this command"));
        }

        Player player = (Player) sender;

        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("help")) {
                help(sender);
            } else if (args[0].equalsIgnoreCase("reload")) {
                reload(sender);
            } else if (args[0].equalsIgnoreCase("get")) {
                get(sender, args);
            } else if (args[0].equalsIgnoreCase("delete")) {
                delete(sender, args);
            } else if (args[0].equalsIgnoreCase("ban")) {
                ban(sender, args);
            } else if (args[0].equalsIgnoreCase("clear")) {
                clear(sender, args);
            }
        } else {
            help(sender);
            return true;
        }
        return false;
    }

    public void help(CommandSender sender) {

        Player player = (Player) sender;

            sender.sendMessage
                    (MessageUtils.colorMsg("&2-------&C&lSTAFF &F&LCOMMANDS&2-------"));
            sender.sendMessage
                    (MessageUtils.colorMsg("&2&LGET <&eAUTHOR&2/&eVERSION&2>: &FSee the author/version."));
            sender.sendMessage
                    (MessageUtils.colorMsg("&2&LRELOAD: &FReload all the plugin."));
            sender.sendMessage
                    (MessageUtils.colorMsg("&2&LDELETE: &FDelete an users clan."));
            sender.sendMessage
                    (MessageUtils.colorMsg("&2&LBAN: &FTemporaly bans a clan."));
            sender.sendMessage
                    (MessageUtils.colorMsg("&2&LCLEAR: &FDelete all the Data.yml."));
            sender.sendMessage
                    (MessageUtils.colorMsg("&C&L--------WARNING--------"));
            sender.sendMessage
                    (MessageUtils.colorMsg("&CIf you don't put in the time the clan will be eliminated."));
            sender.sendMessage
                    (MessageUtils.colorMsg("&CIf you use the clear command all clans on the server will be deleted."));
            sender.sendMessage
                    (MessageUtils.colorMsg("&2-------&C&LSTAFF &F&LCOMMANDS&2-------"));
    }

    public void clear(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&c&lUSE: &f/clansx clear"));
            return;
        }

        MainConfigManager mainConfigManager = plugin.getMainConfigManager();
        FileConfiguration dataConfig = mainConfigManager.getData();

        dataConfig.set("Clans", null);

        mainConfigManager.saveData();
        sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&2All clans have been eliminated."));
    }

    public void get(CommandSender sender, String[] args) {
        if (args.length == 1) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&c&lUSE: &f/clansx get <&5author&e/&5version&e>"));
            return;
        }
        if (args[1].equalsIgnoreCase("author")) {
            sender.sendMessage
                    (MessageUtils.colorMsg("&2----------"));
            sender.sendMessage
                    (MessageUtils.colorMsg("&eThe Creator of &l" + ClansX.prefix));
            sender.sendMessage
                    (MessageUtils.colorMsg("&eis &5&l" + plugin.getDescription().getAuthors()));
            sender.sendMessage
                    (MessageUtils.colorMsg("&2----------"));
        } else if (args[1].equalsIgnoreCase("version")) {
            sender.sendMessage
                    (MessageUtils.colorMsg(ClansX.prefix + "&2&lThe version of the plugin is the: &e&lv" + plugin.getDescription().getVersion()));
        }
    }

    public void reload(CommandSender sender) {
        plugin.getMainConfigManager().reloadConfig();
        plugin.getMainConfigManager().reloadData();
        sender.sendMessage
                (MessageUtils.colorMsg(ClansX.prefix + "&2All plugin has been reloaded!"));
    }

    public void delete(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&CUSE: &f/clans delete (name)"));
            return;
        }
        if (args.length == 3) {
            String clanName = args[1];

            MainConfigManager mainConfigManager = plugin.getMainConfigManager();
            FileConfiguration dataConfig = mainConfigManager.getData();

            dataConfig.set("Clans." + clanName, null);

            mainConfigManager.saveData();
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&2The clan &e&l" + clanName + "&2 has been deleted"));
        }
    }

    public void ban(CommandSender sender, String[] args) {
        if (args.length < 2 || args.length > 3) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&c&lUSE: &f/clansx ban <clanName> [duration]"));
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&c&lIf you don't put in the time the clan will be eliminated"));
            return;
        }

        String clanName = args[1];
        long banDuration = -1;

        if (args.length == 3) {
            try {
                banDuration = parseDuration(args[2]);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cInvalid ban duration format. Use numbers followed by 'm' for minutes, 'h' for hours, or 'd' for days (e.g., 5d for 5 days)."));
                return;
            }
        }

        FileConfiguration dataConfig = plugin.getMainConfigManager().getData();
        if (banDuration > 0) {
            long unbanTime = System.currentTimeMillis() + banDuration * 1000;
            dataConfig.set("Clans." + clanName + ".UnbanTime", unbanTime);
        } else {
            dataConfig.set("Clans." + clanName, null);
        }

        plugin.getMainConfigManager().saveData();
        if (banDuration > 0) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cClan '" + clanName + "' has been banned for " + formatDuration(banDuration) + "."));
        } else {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cClan '" + clanName + "' has been permanently banned."));
        }
    }

    private long parseDuration(String duration) {
        char timeUnit = duration.charAt(duration.length() - 1);
        long timeValue = Long.parseLong(duration.substring(0, duration.length() - 1));

        switch (timeUnit) {
            case 'm':
                return timeValue * 60;
            case 'h':
                return timeValue * 3600;
            case 'd':
                return timeValue * 86400;
            case 'w':
                return timeValue * 604800;
            default:
                throw new IllegalArgumentException("Invalid time unit");
        }
    }

    private String formatDuration(long seconds) {
        long weeks = seconds / 604800;
        long days = seconds / 86400;
        seconds %= 86400;
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;

        StringBuilder formattedDuration = new StringBuilder();
        if (days > 0) {
            formattedDuration.append(days).append(" d ");
        }
        if (hours > 0) {
            formattedDuration.append(hours).append(" h ");
        }
        if (minutes > 0) {
            formattedDuration.append(minutes).append(" m ");
        }
        if (weeks > 0) {
            formattedDuration.append(minutes).append(" w ");
        }
        return formattedDuration.toString();
    }
}