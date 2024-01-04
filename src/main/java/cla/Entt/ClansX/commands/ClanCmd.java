package cla.Entt.ClansX.commands;

import cla.Entt.ClansX.ClansX;
import cla.Entt.ClansX.config.MainConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import cla.Entt.ClansX.utils.MessageUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClanCmd implements CommandExecutor {
    private ClansX plugin;

    public ClanCmd(ClansX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        MainConfigManager mainConfigManager = plugin.getMainConfigManager();
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cThe console can't use this command"));
            return true;
        }

        if (!(sender.hasPermission("clansx.clans"))) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cYou don't have permissions to use this command"));
        }

        Player player = (Player) sender;
        String playerName = player.getName();

        String playerClan = getPlayerClan(playerName);

        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("create")) {
                if (playerClan != null && !playerClan.isEmpty()) {
                    sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cYou are already in a clan."));
                    return true;
                }
                create(sender, args);
            } else if (args[0].equalsIgnoreCase("delete")) {
                delete(sender, args, playerClan);
            } else if (args[0].equalsIgnoreCase("report")) {
                if (args.length < 3) {
                    sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cUSE: /clans report <clanName> <reason>"));
                    return true;
                }
                String reportedClan = args[1];
                String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                report(sender, reportedClan, reason);
            } else if (args[0].equalsIgnoreCase("list")) {
                list(sender);
            } else if (args[0].equalsIgnoreCase("join")) {
                if (args.length != 2) {
                    sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cUSE: /clans join <clanName>"));
                    return true;
                }
                String clanToJoin = args[1];
                joinClan(sender, player, playerName, clanToJoin);
            } else if (args[0].equalsIgnoreCase("war")) {
                wars(sender, args, playerClan);
            } else if (args[0].equalsIgnoreCase("expel")) {
                expel(sender, args);
            } else if (args[0].equalsIgnoreCase("invite")) {
                if (args.length != 2) {
                    sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cUse: /clans invite <playerName>"));
                    return true;
                }
                String playerToInvite = args[1];
                inviteToClan(sender, playerToInvite);
            } else if (args[0].equalsIgnoreCase("chat")) {
                if (playerClan == null || playerClan.isEmpty()) {
                    sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cYou are not in a clan."));
                    return true;
                }
                clanMessage(playerClan, player, Arrays.copyOfRange(args, 1, args.length));
            } else if (args[0].equalsIgnoreCase("vote")) {
                vote(sender, args);
            } else if (args[0].equalsIgnoreCase("leave")) {
                leaveClan(sender, playerName, playerClan);
            } else if (args[0].equalsIgnoreCase("stats")) {
                if (playerClan == null || playerClan.isEmpty()) {
                    sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cYou are not in a clan."));
                    return true;
                }
                stats(sender, playerClan);
            } else if (args[0].equalsIgnoreCase("help")) {
                help(sender);
            }
        } else {
            help(sender);
            return true;
        }
        return false;
    }

    public void help(CommandSender sender) {
        sender.sendMessage(MessageUtils.colorMsg("&2-------&3&LCOMMANDS&2-------"));
        sender.sendMessage(MessageUtils.colorMsg("&2&LCREATE: &FCREATE A NEW CLAN"));
        sender.sendMessage(MessageUtils.colorMsg("&2&LDELETE: &FDELETE YOUR CLAN"));
        sender.sendMessage(MessageUtils.colorMsg("&2&LLEAVE: &FLEAVE FROM A CLAN"));
        sender.sendMessage(MessageUtils.colorMsg("&2&LVOTE <LEADER/EXPEL>: &FVOTE A NEW LEADER OR THE EXPEL OF A PLAYER"));
        sender.sendMessage(MessageUtils.colorMsg("&2&LEXPEL: &FEXPEL A PLAYER FROM THE CLAN"));
        sender.sendMessage(MessageUtils.colorMsg("&2&LCHAT: &FCHAT WITH THE REST OF THE CLAN"));
        sender.sendMessage(MessageUtils.colorMsg("&2&LREPORT: &FREPORT A CLAN"));
        sender.sendMessage(MessageUtils.colorMsg("&2&LJOIN: &FJOIN TO A PUBLIC CLAN"));
        sender.sendMessage(MessageUtils.colorMsg("&2&LINVITE: &FINVITE A PLAYER TO THE CLAN"));
        sender.sendMessage(MessageUtils.colorMsg("&2&LLIST: &FSEE ALL THE CLANS OF THE SERVER/WORLD"));
        sender.sendMessage(MessageUtils.colorMsg("&2&LSTATS: &FSEE THE CLAN STATS"));
        sender.sendMessage(MessageUtils.colorMsg(" "));
        sender.sendMessage(MessageUtils.colorMsg("&2-------&3&LINTERACTION&2-------"));
        sender.sendMessage(MessageUtils.colorMsg("&2&LDECLARE: &FDECLARE THE WAR TO OTHER CLAN"));
        sender.sendMessage(MessageUtils.colorMsg("&2&LACCEPT: &FACCEPT THE PEACE OR ALLIANCE WITH OTHER CLAN"));
        sender.sendMessage(MessageUtils.colorMsg("&2&LDENY: &FDENY A PEACE OR ALLIANCE REQUEST"));
        sender.sendMessage(MessageUtils.colorMsg("&2&LPEACE: &FSEND A PEACE REQUEST TO THE ENEMY CLAN"));
        sender.sendMessage(MessageUtils.colorMsg("&2&LALLIANCE: &FSEND A ALLIANCE REQUEST TO OTHER CLAN"));
        sender.sendMessage(MessageUtils.colorMsg("&2-------&3&LCOMMANDS&2-------"));
    }

    public void expel(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String playerName = args[1];
        String clanName = getPlayerClan(playerName);

        if (args.length != 2) {
            player.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&CUSE: &f/clans expel <playerName>"));
            return;
        }
        if (clanName == null || clanName.isEmpty()) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cYou are not in a clan."));
            return;
        }

        MainConfigManager mainConfigManager = plugin.getMainConfigManager();
        FileConfiguration dataConfig = mainConfigManager.getData();

        List<String> users = dataConfig.getStringList("Clans." + clanName + ".Users");

        String leader = dataConfig.getString("Clans." + clanName + ".Leader");
        if (player.getName().equalsIgnoreCase(leader)) {
            if (args[1].equalsIgnoreCase(player.getName())) {
                player.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cyou can't expel yourself &e" + player.getName()));
                player.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cIf you want to leave the clan use &e/clans leave"));
            } else {
                if (users.contains(playerName)) {
                    users.remove(playerName);
                    dataConfig.set("Clans." + clanName + ".Users", users);
                    plugin.getMainConfigManager().saveData();

                    player.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&2Player &e&l" + playerName + " &2has been expelled from the clan &e&l" + clanName));

                    if (users.isEmpty()) {
                        dataConfig.set("Clans." + clanName, null);
                        plugin.getMainConfigManager().saveData();
                        player.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&2The clan is empty, it has been eliminated"));
                    }
                } else {
                    player.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cPlayer is not a member of any clan."));
                }
            }
        } else {
            player.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cOnly the clan leader can expel members."));
        }
    }

    public void vote(CommandSender sender, String[] args) {
        MainConfigManager mainConfigManager = plugin.getMainConfigManager();
        FileConfiguration dataConfig = mainConfigManager.getData();

        if (args.length != 3) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&CUSE: &f/clansx vote <&5expel&e/&5leader&e> <&5player name&e>"));
            return;
        }

        String playerClan = getPlayerClan(sender.getName());
        if (playerClan == null || playerClan.isEmpty()) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cYou are not in a clan."));
            return;
        }

        if (!dataConfig.contains("Clans." + playerClan)) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cThe specified clan does not exist."));
            return;
        }

        String voteType = args[1].toLowerCase();
        String playerName = args[2];

        String votedUsersPath = "Clans." + playerClan + ".voting.usersVoted." + playerName;

        int votes = dataConfig.getInt(votedUsersPath, 0);
        dataConfig.set(votedUsersPath, votes + 1);

        mainConfigManager.saveData();

        sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&2Your vote to " + voteType + " " + playerName + " has been registered."));

        updateVotesFormat(playerClan, dataConfig);
    }

    private void updateVotesFormat(String clanName, FileConfiguration dataConfig) {
        MainConfigManager mainConfigManager = plugin.getMainConfigManager();

        ConfigurationSection votesSection = dataConfig.getConfigurationSection("Clans." + clanName + ".voting.usersVoted");

        if (votesSection != null) {
            for (String votedUser : votesSection.getKeys(false)) {
                int votes = dataConfig.getInt("Clans." + clanName + ".voting.VotedUser." + votedUser, 0);
                dataConfig.set("Clans." + clanName + ".voting.VotedUser." + votedUser, votedUser + " (" + votes + ")");
            }

            plugin.getMainConfigManager().saveData();
        }
    }

    public void wars(CommandSender sender, String[] args, String playerClan) {
        if (args.length < 2) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&CUSE: &f/clans wars <declare/peace/alliance> <clanName>"));
            return;
        }
        if (playerClan == null || playerClan.isEmpty()) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cYou are not in a clan."));
            return;
        }

        Player player = (Player) sender;
        String playerName = player.getName();

        MainConfigManager mainConfigManager = plugin.getMainConfigManager();
        FileConfiguration dataConfig = mainConfigManager.getData();

        String leader = dataConfig.getString("Clans." + playerClan + ".Leader");

        if (!playerName.equalsIgnoreCase(leader)) {
            sender.sendMessage
                    (MessageUtils.colorMsg(ClansX.prefix + "&cYou are not the leader of this clan."));
            return;
        } else {
            if (args[2].equalsIgnoreCase("peace")) {
                if (args.length < 4) {
                    sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cYou need to specify a clan name to offer peace."));
                    return;
                }
                String otherClan = args[3];

                if (!dataConfig.contains("Clans." + otherClan)) {
                    sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cThe specified clan does not exist."));
                    return;
                }

                List<String> enemy = dataConfig.getStringList("Wars." + otherClan + ".Enemy");
                enemy.remove(playerClan);
                dataConfig.set("Wars." + otherClan + ".Enemy", enemy);

                mainConfigManager.saveData();

                sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&2You have offered peace to: &e" + otherClan));
            }
            if (args[2].equalsIgnoreCase("declare")) {
                if (args.length < 4) {
                    sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cYou need to specify a clan name to declare the war."));
                    return;
                }
                String otherClan = args[3];

                if (!dataConfig.contains("Clans." + otherClan)) {
                    sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cThe specified clan does not exist."));
                    return;
                }

                List<String> enemy = dataConfig.getStringList("Wars." + otherClan + ".Enemy");
                enemy.add(playerClan);
                dataConfig.set("Wars." + otherClan + ".Enemy", enemy);

                List<String> pendingOtherClan = dataConfig.getStringList("Wars." + otherClan + ".Ally.Pending");
                pendingOtherClan.remove(playerClan);
                dataConfig.set("Wars." + otherClan + ".Ally.Pending", pendingOtherClan);

                List<String> allianceOtherClan = dataConfig.getStringList("Wars." + otherClan + ".Ally.Alliance");
                allianceOtherClan.remove(playerClan);
                dataConfig.set("Wars." + otherClan + ".Ally.Alliance", allianceOtherClan);

                mainConfigManager.saveData();

                List<String> playerEnemy = dataConfig.getStringList("Wars." + playerClan + ".Enemy");
                playerEnemy.add(otherClan);
                dataConfig.set("Wars." + playerClan + ".Enemy", playerEnemy);

                sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&2Your clan has started a war with: &e" + otherClan));
            }
            if (args[2].equalsIgnoreCase("alliance")) {
                if (args.length < 3) {
                    sender.sendMessage
                            (MessageUtils.colorMsg(ClansX.prefix + "&cYou need to specify a clan name to declare the war."));
                }
                String otherClan = args[3];

                if (!dataConfig.contains("Clans." + otherClan)) {
                    sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cThe specified clan does not exist."));
                    return;
                }
                List<String> pending = dataConfig.getStringList("Wars." + otherClan + ".Ally");
                pending.add(playerClan);
                dataConfig.set("Wars." + otherClan + ".Ally" + ".Pending", pending);
                mainConfigManager.saveData();

                sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&2Alliance request sent to: &e" + otherClan));
            }
            if (args[2].equalsIgnoreCase("peace")) {
                String otherClan = args[3];

                if (!dataConfig.contains("Clans." + otherClan)) {
                    sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cThe specified clan does not exist."));
                    return;
                }
                List<String> pending = dataConfig.getStringList("Wars." + otherClan + ".Ally");
                pending.add(playerClan);
                dataConfig.set("Wars." + otherClan + ".Ally" + ".Pending", pending);
                mainConfigManager.saveData();

                sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&2You send a peace offer to: &e" + otherClan));
            }
            if (args[2].equalsIgnoreCase("accept")) {
                if (args.length < 4) {
                    sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cYou need to specify a clan name to accept the alliance."));
                    return;
                }
                String otherClan = args[3];

                if (!dataConfig.contains("Clans." + otherClan)) {
                    sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cThe specified clan does not exist."));
                    return;
                }

                List<String> ally = dataConfig.getStringList("Wars." + otherClan + ".Ally.Alliance");
                ally.add(playerClan);
                dataConfig.set("Wars." + otherClan + ".Ally.Alliance", ally);

                List<String> pending = dataConfig.getStringList("Wars." + otherClan + ".Ally.Pending");
                pending.remove(playerClan);
                dataConfig.set("Wars." + otherClan + ".Ally.Pending", pending);

                mainConfigManager.saveData();

                sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&2Now you are allied with: &e" + otherClan));
            }
            if (args[2].equalsIgnoreCase("deny")) {
                if (args.length < 4) {
                    sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cYou need to specify a clan name to deny the alliance."));
                    return;
                }
                String otherClan = args[3];

                if (!dataConfig.contains("Clans." + otherClan)) {
                    sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cThe specified clan does not exist."));
                    return;
                }

                List<String> pending = dataConfig.getStringList("Wars." + otherClan + ".Ally.Pending");
                pending.remove(playerClan);
                dataConfig.set("Wars." + otherClan + ".Ally.Pending", pending);

                mainConfigManager.saveData();

                sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&2You have denied the alliance request from: &e" + otherClan));
            }
        }
    }

    public void stats(CommandSender sender, String clanName) {
        MainConfigManager mainConfigManager = plugin.getMainConfigManager();
        FileConfiguration dataConfig = mainConfigManager.getData();

        if (!dataConfig.contains("Clans." + clanName)) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cThe specified clan does not exist."));
            return;
        }

        String founder = dataConfig.getString("Clans." + clanName + ".Founder");
        String leader = dataConfig.getString("Clans." + clanName + ".Leader");
        List<String> users = dataConfig.getStringList("Clans." + clanName + ".Users");

        sender.sendMessage(MessageUtils.colorMsg("&2--------&f&lCLAN STATS&2--------"));
        sender.sendMessage(MessageUtils.colorMsg("&2Name: &e&l" + clanName));
        sender.sendMessage(MessageUtils.colorMsg("&2Founder: &e&l" + (founder != null ? founder : "Unknown")));
        sender.sendMessage(MessageUtils.colorMsg("&2Leader: &e&l" + (leader != null ? leader : "Unknown")));
        sender.sendMessage(MessageUtils.colorMsg("&2Users:"));
        for (String user : users) {
            sender.sendMessage(MessageUtils.colorMsg("&f- &l" + user));
        }
        sender.sendMessage(MessageUtils.colorMsg("&2--------&f&lCLAN STATS&2--------"));
    }

    private void inviteToClan(CommandSender sender, String playerToInvite) {
        Player invitedPlayer = plugin.getServer().getPlayer(playerToInvite);
        if (invitedPlayer == null || !invitedPlayer.isOnline()) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cThe player is not online or doesn't exist."));
            return;
        }
        sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&2Invitation sent to: &e" + playerToInvite));
    }

    public void clanMessage(String clanName, Player player, String[] message) {
        MainConfigManager mainConfigManager = plugin.getMainConfigManager();
        FileConfiguration dataConfig = mainConfigManager.getData();

        List<String> users = dataConfig.getStringList("Clans." + clanName + ".Users");

        for (String userName : users) {
            Player recipient = plugin.getServer().getPlayer(userName);
            if (recipient != null) {
                if (recipient != player) {
                    String formattedMessage = String.join(" ", message);
                    recipient.sendMessage(MessageUtils.colorMsg(ClansX.prefix + player.getName() + "&f: &7" + formattedMessage));
                }
            }
        }
    }

    private void leaveClan(CommandSender sender, String playerName, String playerClan) {
        if (playerClan == null || playerClan.isEmpty()) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cYou are not in a clan."));
            return;
        }

        MainConfigManager mainConfigManager = plugin.getMainConfigManager();
        FileConfiguration dataConfig = mainConfigManager.getData();

        List<String> users = dataConfig.getStringList("Clans." + playerClan + ".Users");
        users.remove(playerName);
        dataConfig.set("Clans." + playerClan + ".Users", users);

        String leader = dataConfig.getString("Clans." + playerClan + ".Leader");
        if (playerName.equals(leader)) {
            if (users.isEmpty()) {
                dataConfig.set("Clans." + playerClan, null);
                mainConfigManager.saveData();
                sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cThe clan has been deleted because there are no remaining members."));
            } else {
                String founder = dataConfig.getString("Clans." + playerClan + ".Founder");
                if (users.contains(founder)) {
                    dataConfig.set("Clans." + playerClan + ".Leader", founder);
                    mainConfigManager.saveData();
                    sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cYou have left the clan. New leader: " + founder));
                    return;
                }
                dataConfig.set("Clans." + playerClan + ".Leader", null);
                mainConfigManager.saveData();
                sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cYou have left the clan. The clan currently has no leader."));
            }
        } else {
            mainConfigManager.saveData();
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&2You have left the clan."));
        }
    }

    private void joinClan(CommandSender sender, Player player, String playerName, String clanToJoin) {
        if (getPlayerClan(playerName) != null) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cYou are already in a clan."));
            return;
        }

        MainConfigManager mainConfigManager = plugin.getMainConfigManager();
        FileConfiguration dataConfig = mainConfigManager.getData();
        if (!dataConfig.contains("Clans." + clanToJoin)) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cThe specified clan does not exist."));
            return;
        }

        List<String> users = dataConfig.getStringList("Clans." + clanToJoin + ".Users");
        users.add(playerName);
        dataConfig.set("Clans." + clanToJoin + ".Users", users);
        mainConfigManager.saveData();

        sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&2You have joined the clan: &e" + clanToJoin));
    }

    private String getPlayerClan(String playerName) {
        MainConfigManager mainConfigManager = plugin.getMainConfigManager();
        FileConfiguration dataConfig = mainConfigManager.getData();

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

    public void list(CommandSender sender) {
        MainConfigManager mainConfigManager = plugin.getMainConfigManager();
        FileConfiguration dataConfig = mainConfigManager.getData();

        ConfigurationSection clansSection = dataConfig.getConfigurationSection("Clans");
        if (clansSection == null || clansSection.getKeys(false).isEmpty()) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cThere are no clans on the server."));
            return;
        }

        sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&fClans on the server:"));
        for (String clanName : clansSection.getKeys(false)) {
            sender.sendMessage(MessageUtils.colorMsg("&f- " + clanName));
        }
    }

    public void report(CommandSender sender, String reportedClan, String reason) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cOnly players can use this command."));
            return;
        }
        Player reporter = (Player) sender;

        MainConfigManager mainConfigManager = plugin.getMainConfigManager();
        FileConfiguration dataConfig = mainConfigManager.getData();

        if (!dataConfig.contains("Clans." + reportedClan)) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cThe reported clan does not exist."));
            return;
        }
        String reporterName = reporter.getName();
        String reporterClan = getPlayerClan(reporterName);

        String reportedState = "reported";

        if (reason == null || reason.isEmpty()) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cPlease provide a valid reason for the report."));
            return;
        }
        List<String> reports = dataConfig.getStringList("Clans." + reportedClan + ".Reports");
        reports.add(reason);
        dataConfig.set("Clans." + reportedClan + ".Reports", reports);

        dataConfig.set("Clans." + reportedClan + ".State", reportedState);
        dataConfig.set("Clans." + reportedClan + ".ReportReason", reason);

        mainConfigManager.saveData();

        sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&2Clan reported: &e" + reportedClan + ". Reason: " + reason));
    }

    public void delete(CommandSender sender, String[] args, String playerClan) {
        if (playerClan == null || playerClan.isEmpty()) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cYou are not in a clan."));
            return;
        }

        Player player = (Player) sender;
        String playerName = player.getName();

        MainConfigManager mainConfigManager = plugin.getMainConfigManager();
        FileConfiguration dataConfig = mainConfigManager.getData();

        String leader = dataConfig.getString("Clans." + playerClan + ".Leader");

        if (!playerName.equalsIgnoreCase(leader)) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cYou are not the leader of this clan."));
            return;
        }

        dataConfig.set("Clans." + playerClan, null);

        mainConfigManager.saveData();
        sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cYour clan has been deleted."));
    }

    public void create(CommandSender sender, String[] args) {
        if (args.length == 1) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&CUSE: &f/clans create &5(name of your clan)."));
            return;
        }

        String clanName = args[1];
        MainConfigManager mainConfigManager = plugin.getMainConfigManager();

        if (!mainConfigManager.isNamesBlockedEnabled()) {
            createClan(sender, clanName);
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&2Your clan &e" + clanName + " &2has been created."));
            return;
        }

        List<String> blockedNames = mainConfigManager.namesBlocked();

        if (blockedNames.contains(clanName.toLowerCase())) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cThis name is not allowed for a clan."));
            return;
        }

        if (clanExists(clanName)) {
            sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&cA clan with that name already exists."));
            return;
        }

        createClan(sender, clanName);
        sender.sendMessage(MessageUtils.colorMsg(ClansX.prefix + "&2Your clan &e" + clanName + " &2has been created."));
    }

    private boolean clanExists(String clanName) {
        MainConfigManager mainConfigManager = plugin.getMainConfigManager();
        FileConfiguration dataConfig = mainConfigManager.getData();
        return dataConfig.getConfigurationSection("Clans." + clanName) != null;

    }

    private void createClan(CommandSender sender, String clanName) {
        MainConfigManager mainConfigManager = plugin.getMainConfigManager();
        FileConfiguration dataConfig = mainConfigManager.getData();

        String playerName = sender.getName();

        dataConfig.set("Clans." + clanName + ".Founder", playerName);
        dataConfig.set("Clans." + clanName + ".Leader", playerName);
        dataConfig.set("Clans." + clanName + ".Users", Collections.singletonList(playerName));

        mainConfigManager.saveData();
    }
}