package x.Entt.ClansX.CMDs;

import org.jetbrains.annotations.NotNull;

import x.Entt.ClansX.CX;
import x.Entt.ClansX.Utils.FileHandler;
import x.Entt.ClansX.Utils.MSG;
import static x.Entt.ClansX.CX.econ;
import static x.Entt.ClansX.CX.prefix;

import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.command.TabCompleter;

public class CCMD implements CommandExecutor, TabCompleter {
   private final CX plugin;

   public CCMD(CX plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(MSG.color(prefix + "&cConsole Commands: &f/cls reload."));
         return true;
      } else {
         if (!sender.hasPermission("cx.user")) {
            sender.sendMessage(MSG.color(prefix + "&cYou don't have permissions to use this command"));
         }

         Player player = (Player)sender;
         String playerName = player.getName();
         String playerClan = this.getPlayerClan(playerName);
         if (args.length < 1) {
            this.help(sender);
            return true;
         } else {
            if (args[0].equalsIgnoreCase("create")) {
               if (playerClan != null && !playerClan.isEmpty()) {
                  sender.sendMessage(MSG.color(prefix + "&cYou are already in a clan."));
                  return true;
               }

               this.create(sender, args);
            } else if (args[0].equalsIgnoreCase("disband")) {
               this.disband(sender, playerClan);
            } else {
               String playerToInvite;
               if (args[0].equalsIgnoreCase("report")) {
                  if (args.length < 3) {
                     sender.sendMessage(MSG.color(prefix + "&cUSE: /cls report <clan> <reason>"));
                     return true;
                  }

                  playerToInvite = args[1];
                  String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                  this.report(sender, playerToInvite, reason);
               } else if (args[0].equalsIgnoreCase("list")) {
                  this.list(sender);
               } else if (args[0].equalsIgnoreCase("join")) {
                  if (args.length != 2) {
                     sender.sendMessage(MSG.color(prefix + "&cUSE: /cls join <clan>"));
                     return true;
                  }

                  playerToInvite = args[1];
                  this.joinClan(sender, playerName, playerToInvite);
               } else if (args[0].equalsIgnoreCase("war")) {
                  this.wars(sender, args, playerClan);
               } else if (args[0].equalsIgnoreCase("kick")) {
                  this.kick(sender, args);
               } else if (args[0].equalsIgnoreCase("invite")) {
                  if (args.length != 2) {
                     sender.sendMessage(MSG.color(prefix + "&cUse: /cls invite <player>"));
                     return true;
                  }

                  playerToInvite = args[1];
                  this.inviteToClan(sender, playerToInvite);
               } else if (args[0].equalsIgnoreCase("chat")) {
                  if (playerClan == null || playerClan.isEmpty()) {
                     sender.sendMessage(MSG.color(prefix + "&cYou are not in a clan."));
                     return true;
                  }

                  this.chat(playerClan, player, Arrays.copyOfRange(args, 1, args.length));
               } else if (args[0].equalsIgnoreCase("leave")) {
                  this.leave(sender, playerClan);
               } else if (args[0].equalsIgnoreCase("stats")) {
                  if (playerClan == null || playerClan.isEmpty()) {
                     sender.sendMessage(MSG.color(prefix + "&cYou are not in a clan."));
                     return true;
                  }

                  this.stats(sender, playerClan);
               } else if (args[0].equalsIgnoreCase("resign")) {
                  this.resign(sender, playerClan);
               }
            }

            return false;
         }
      }
   }

    public void help(CommandSender sender) {
        sender.sendMessage(MSG.color("&6======= &lCLAN COMMANDS &6======="));
        sender.sendMessage(MSG.color("&3&lCREATE: &fCreate a new clan and start your adventure!"));
        sender.sendMessage(MSG.color("&3&lJOIN: &fJoin a public clan and make new friends!"));
        sender.sendMessage(MSG.color("&3&lINVITE: &fInvite a player to become part of your clan!"));
        sender.sendMessage(MSG.color("&3&lLEAVE: &fLeave your current clan respectfully."));
        sender.sendMessage(MSG.color("&3&lDISBAND: &fDisband your clan if necessary."));
        sender.sendMessage(MSG.color("&3&lKICK: &fRemove a player from your clan if needed."));
        sender.sendMessage(MSG.color("&3&lCHAT: &fTalk with your clan members easily."));
        sender.sendMessage(MSG.color("&3&lSTATS: &fView your clan's achievements and stats."));
        sender.sendMessage(MSG.color("&3&lLIST: &fSee all clans available on the server."));
        sender.sendMessage(MSG.color("&3&lREPORT: &fReport a clan for any issues."));
        sender.sendMessage(MSG.color("&3&lRESIGN: &fResign from your position as leader gracefully."));
        sender.sendMessage(MSG.color(" "));
        sender.sendMessage(MSG.color("&6======= &lWAR COMMANDS &6======="));
        sender.sendMessage(MSG.color("&4&lDECLARE: &fDeclare war on another clan!"));
        sender.sendMessage(MSG.color("&4&lPEACE: &fSend a peace request to an enemy clan."));
        sender.sendMessage(MSG.color("&4&lACCEPT: &fAccept a peace or alliance request."));
        sender.sendMessage(MSG.color("&4&lDENY: &fDeny a peace or alliance proposal."));
        sender.sendMessage(MSG.color("&4&lALLIANCE: &fSend an alliance request to another clan."));
        sender.sendMessage(MSG.color("&6======= " + prefix + " &6======="));
    }

    public void kick(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(MSG.color(prefix + "&c&lUSE:&f /cls kick <player>"));
            return;
        }

        String playerName = args[1];
        Player player = (Player) sender;
        FileHandler fh = plugin.getFH();
        String clanName = this.getPlayerClan(player.getName());

        if (clanName == null || clanName.isEmpty()) {
            sender.sendMessage(MSG.color(prefix + "&cYou are not in a clan."));
            return;
        }

        FileConfiguration dataConfig = fh.getData();
        List<String> users = dataConfig.getStringList("Clans." + clanName + ".Users");
        String leader = dataConfig.getString("Clans." + clanName + ".Leader");

        if (!player.getName().equalsIgnoreCase(leader)) {
            sender.sendMessage(MSG.color(prefix + "&cOnly the clan leader can expel members."));
            return;
        }

        if (playerName.equalsIgnoreCase(player.getName())) {
            sender.sendMessage(MSG.color(prefix + "&cYou can't expel yourself, use /clans leave."));
            return;
        }

        if (!users.contains(playerName)) {
            sender.sendMessage(MSG.color(prefix + "&cPlayer is not a member of the clan."));
            return;
        }

        users.remove(playerName);
        dataConfig.set("Clans." + clanName + ".Users", users);
        fh.saveData();
        sender.sendMessage(MSG.color(prefix + "&2Player &e&l" + playerName + " &2has been expelled from the clan &e&l" + clanName));

        if (users.isEmpty()) {
            dataConfig.set("Clans." + clanName, null);
            fh.saveData();
            sender.sendMessage(MSG.color(prefix + "&2The clan is empty, it has been eliminated."));
        }
    }

    public void resign(CommandSender sender, String playerClan) {
      if (playerClan != null && !playerClan.isEmpty()) {
         FileHandler fh = plugin.getFH();
         FileConfiguration dataConfig = fh.getData();
         List<String> users = dataConfig.getStringList("Clans." + playerClan + ".Users");
         if (!users.isEmpty()) {
            int randomIndex = (new Random()).nextInt(users.size());
            String newLeader = users.get(randomIndex);
            dataConfig.set("Clans." + playerClan + ".Leader", newLeader);
            sender.sendMessage(MSG.color(prefix + "&cYou reject your position as clan leader! The new leader is " + newLeader));
         } else {
            dataConfig.set("Clans." + playerClan, null);
            sender.sendMessage(MSG.color(prefix + "&cClan deleted! &7(cause: 0 members)."));
         }

      } else {
         sender.sendMessage(MSG.color(prefix + "&cYou are not in a clan."));
      }
   }

    public void wars(CommandSender sender, String[] args, String playerClan) {
        if (args.length < 2) {
            sender.sendMessage(MSG.color(prefix + "&c&lUSE:&f /clans wars <declare/peace/alliance/accept/deny> <clan>"));
            return;
        }

        if (playerClan == null || playerClan.isEmpty()) {
            sender.sendMessage(MSG.color(prefix + "&cYou are not in a clan."));
            return;
        }

        Player player = (Player) sender;
        String playerName = player.getName();
        FileHandler fh = plugin.getFH();
        FileConfiguration dataConfig = fh.getData();
        String leader = dataConfig.getString("Clans." + playerClan + ".Leader");

        if (!playerName.equalsIgnoreCase(leader)) {
            sender.sendMessage(MSG.color(prefix + "&cYou are not the leader of this clan."));
            return;
        }

        String action = args[1].toLowerCase();
        String otherClan;

        switch (action) {
            case "peace":
                if (args.length < 3) {
                    sender.sendMessage(MSG.color(prefix + "&cYou need to specify a clan name to offer peace."));
                    return;
                }
                otherClan = args[2];
                handlePeaceOffer(sender, playerClan, otherClan, dataConfig, fh);
                break;

            case "declare":
                if (args.length < 3) {
                    sender.sendMessage(MSG.color(prefix + "&cYou need to specify a clan name to declare war."));
                    return;
                }
                otherClan = args[2];
                handleWarDeclaration(sender, playerClan, otherClan, dataConfig, fh);
                break;

            case "alliance":
                if (args.length < 3) {
                    sender.sendMessage(MSG.color(prefix + "&cYou need to specify a clan name to declare an alliance."));
                    return;
                }
                otherClan = args[2];
                handleAllianceRequest(sender, playerClan, otherClan, dataConfig, fh);
                break;

            case "accept":
                if (args.length < 3) {
                    sender.sendMessage(MSG.color(prefix + "&cYou need to specify a clan name to accept the alliance."));
                    return;
                }
                otherClan = args[2];
                handleAllianceAcceptance(sender, playerClan, otherClan, dataConfig, fh);
                break;

            case "deny":
                if (args.length < 3) {
                    sender.sendMessage(MSG.color(prefix + "&cYou need to specify a clan name to deny the alliance."));
                    return;
                }
                otherClan = args[2];
                handleAllianceDenial(sender, playerClan, otherClan, dataConfig, fh);
                break;

            default:
                sender.sendMessage(MSG.color(prefix + "&cInvalid action. Use: <declare/peace/alliance/accept/deny>"));
                break;
        }
    }

    private void handlePeaceOffer(CommandSender sender, String playerClan, String otherClan, FileConfiguration dataConfig, FileHandler fh) {
        if (!dataConfig.contains("Clans." + otherClan)) {
            sender.sendMessage(MSG.color(prefix + "&cThe specified clan does not exist."));
            return;
        }

        List<String> pending = dataConfig.getStringList("Wars." + otherClan + ".Enemy");
        pending.remove(playerClan);
        dataConfig.set("Wars." + otherClan + ".Enemy", pending);
        fh.saveData();
        sender.sendMessage(MSG.color(prefix + "&2You have offered peace to: &e" + otherClan));
    }

    private void handleWarDeclaration(CommandSender sender, String playerClan, String otherClan, FileConfiguration dataConfig, FileHandler fh) {
        if (!dataConfig.contains("Clans." + otherClan)) {
            sender.sendMessage(MSG.color(prefix + "&cThe specified clan does not exist."));
            return;
        }

        List<String> pending = dataConfig.getStringList("Wars." + otherClan + ".Enemy");
        pending.add(playerClan);
        dataConfig.set("Wars." + otherClan + ".Enemy", pending);

        removePendingAlliance(playerClan, otherClan, dataConfig);

        List<String> playerEnemy = dataConfig.getStringList("Wars." + playerClan + ".Enemy");
        playerEnemy.add(otherClan);
        dataConfig.set("Wars." + playerClan + ".Enemy", playerEnemy);

        fh.saveData();
        sender.sendMessage(MSG.color(prefix + "&2Your clan has started a war with: &e" + otherClan));
    }

    private void handleAllianceRequest(CommandSender sender, String playerClan, String otherClan, FileConfiguration dataConfig, FileHandler fh) {
        if (!dataConfig.contains("Clans." + otherClan)) {
            sender.sendMessage(MSG.color(prefix + "&cNo clan found with that name."));
            return;
        }

        List<String> pending = dataConfig.getStringList("Wars." + otherClan + ".Ally.Pending");
        pending.add(playerClan);
        dataConfig.set("Wars." + otherClan + ".Ally.Pending", pending);

        fh.saveData();
        sender.sendMessage(MSG.color(prefix + "&2Alliance request sent to: &e" + otherClan));
    }

    private void handleAllianceAcceptance(CommandSender sender, String playerClan, String otherClan, FileConfiguration dataConfig, FileHandler fh) {
        if (!dataConfig.contains("Clans." + otherClan)) {
            sender.sendMessage(MSG.color(prefix + "&cThe specified clan does not exist."));
            return;
        }

        List<String> pending = dataConfig.getStringList("Wars." + otherClan + ".Ally.Alliance");
        pending.add(playerClan);
        dataConfig.set("Wars." + otherClan + ".Ally.Alliance", pending);

        pending = dataConfig.getStringList("Wars." + otherClan + ".Ally.Pending");
        pending.remove(playerClan);
        dataConfig.set("Wars." + otherClan + ".Ally.Pending", pending);

        fh.saveData();
        sender.sendMessage(MSG.color(prefix + "&2Now you are allied with: &e" + otherClan));
    }

    private void handleAllianceDenial(CommandSender sender, String playerClan, String otherClan, FileConfiguration dataConfig, FileHandler fh) {
        if (!dataConfig.contains("Clans." + otherClan)) {
            sender.sendMessage(MSG.color(prefix + "&cNo clan found with that name."));
            return;
        }

        List<String> pending = dataConfig.getStringList("Wars." + otherClan + ".Ally.Pending");
        pending.remove(playerClan);
        dataConfig.set("Wars." + otherClan + ".Ally.Pending", pending);

        fh.saveData();
        sender.sendMessage(MSG.color(prefix + "&2You rejected the alliance request of: &e" + otherClan));
    }

    private void removePendingAlliance(String playerClan, String otherClan, FileConfiguration dataConfig) {
        List<String> pending = dataConfig.getStringList("Wars." + otherClan + ".Ally.Pending");
        pending.remove(playerClan);
        dataConfig.set("Wars." + otherClan + ".Ally.Pending", pending);

        List<String> allianceOtherClan = dataConfig.getStringList("Wars." + otherClan + ".Ally.Alliance");
        allianceOtherClan.remove(playerClan);
        dataConfig.set("Wars." + otherClan + ".Ally.Alliance", allianceOtherClan);
    }

    public void stats(CommandSender sender, String clanName) {
      FileHandler fh = plugin.getFH();
      FileConfiguration dataConfig = fh.getData();
      if (!dataConfig.contains("Clans." + clanName)) {
         sender.sendMessage(MSG.color(prefix + "&cNo clan found with that name."));
      } else {
         String founder = dataConfig.getString("Clans." + clanName + ".Founder");
         String leader = dataConfig.getString("Clans." + clanName + ".Leader");
         
         List<String> users = dataConfig.getStringList("Clans." + clanName + ".Users");
         sender.sendMessage(MSG.color("&2--------&f&lSTATS&2--------"));
         sender.sendMessage(MSG.color("&2Name: &e&l" + clanName));
         sender.sendMessage(MSG.color("&2Founder: &e&l" + (founder != null ? founder : "???")));
         sender.sendMessage(MSG.color("&2Leader: &e&l" + (leader != null ? leader : "???")));
         sender.sendMessage(MSG.color("&2Members:"));

          for (String user : users) {
              sender.sendMessage(MSG.color("&f- &l" + user));
          }

         sender.sendMessage(MSG.color("&2-------- "+ prefix + "&2--------"));
      }
   }

   private void inviteToClan(CommandSender sender, String playerToInvite) {
      Player invitedPlayer = this.plugin.getServer().getPlayer(playerToInvite);
      if (invitedPlayer != null && invitedPlayer.isOnline()) {
         sender.sendMessage(MSG.color(prefix + "&2Invitation sent to: &e" + playerToInvite));
      } else {
         sender.sendMessage(MSG.color(prefix + "&cThis player is not online."));
      }
   }

    public void chat(String clanName, Player player, String[] message) {
        FileHandler fh = plugin.getFH();
        FileConfiguration dataConfig = fh.getData();
        List<String> users = dataConfig.getStringList("Clans." + clanName + ".Users");

        String formattedMessage = String.join(" ", message);

        player.sendMessage(MSG.color("&e" + getPlayerClan(player.getName()) + " &f" + player.getName() + "&f: &7" + formattedMessage));

        for (String userName : users) {
            Player recipient = this.plugin.getServer().getPlayer(userName);
            if (recipient != null && recipient != player) {
                recipient.sendMessage(MSG.color("&e" + getPlayerClan(player.getName()) + " &f" + player.getName() + "&f: &7" + formattedMessage));
            }
        }
    }

    private void leave(CommandSender sender, String playerClan) {
        Player player = (Player) sender;

        if (playerClan == null || playerClan.isEmpty()) {
            sender.sendMessage(MSG.color(prefix + "&cYou are not in a clan."));
            return;
        }

        FileHandler fh = plugin.getFH();
        FileConfiguration dataConfig = fh.getData();
        List<String> users = dataConfig.getStringList("Clans." + playerClan + ".Users");
        String playerName = player.getName();
        String leader = dataConfig.getString("Clans." + playerClan + ".Leader");

        users.remove(playerName);
        dataConfig.set("Clans." + playerClan + ".Users", users);

        if (users.isEmpty()) {
            dataConfig.set("Clans." + playerClan, null);
            sender.sendMessage(MSG.color(prefix + "&cClan deleted! &7(cause: 0 members)."));
            fh.saveData();
            return;
        }

        if (playerName.equals(leader)) {
            String newLeader = users.get(new Random().nextInt(users.size()));
            dataConfig.set("Clans." + playerClan + ".Leader", newLeader);
            sender.sendMessage(MSG.color(prefix + "&cYou have left the clan. " + newLeader + " is the new leader."));
        } else {
            sender.sendMessage(MSG.color(prefix + "&2You have left the clan."));
        }

        fh.saveData();
    }

    private void joinClan(CommandSender sender, String playerName, String clanToJoin) {
      if (this.getPlayerClan(playerName) != null) {
         sender.sendMessage(MSG.color(prefix + "&cYou are already in a clan."));
      } else {
         FileHandler fh = plugin.getFH();
         FileConfiguration dataConfig = fh.getData();
         if (!dataConfig.contains("Clans." + clanToJoin)) {
            sender.sendMessage(MSG.color(prefix + "&cThe specified clan does not exist."));
         } else {
            List<String> users = dataConfig.getStringList("Clans." + clanToJoin + ".Users");
            users.add(playerName);
            dataConfig.set("Clans." + clanToJoin + ".Users", users);
            fh.saveData();
            sender.sendMessage(MSG.color(prefix + "&2You have joined the clan: &e" + clanToJoin));
         }
      }
   }

   private String getPlayerClan(String playerName) {
      FileHandler fh = plugin.getFH();
      FileConfiguration dataConfig = fh.getData();
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
        FileHandler fh = plugin.getFH();
        FileConfiguration dataConfig = fh.getData();
        ConfigurationSection clansSection = dataConfig.getConfigurationSection("Clans");

        if (clansSection == null || clansSection.getKeys(false).isEmpty()) {
            sender.sendMessage(MSG.color(prefix + "&cThere are no clans on the server."));
            return;
        }

        StringBuilder clansList = new StringBuilder();
        clansList.append(MSG.color(prefix + "&2&lClans:\n"));

        for (String clan : clansSection.getKeys(false)) {
            clansList.append(MSG.color("&c- ")).append(clan).append("\n");
        }

        clansList.append(MSG.color(prefix + "&c--- end >_< ---"));

        sender.sendMessage(clansList.toString());
    }

    private void report(CommandSender sender, String reportedClan, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            sender.sendMessage(MSG.color(prefix + "&cPlease provide a valid reason for the report."));
            return;
        }

        FileHandler fh = plugin.getFH();
        FileConfiguration dataConfig = fh.getData();

        if (!dataConfig.contains("Clans." + reportedClan)) {
            sender.sendMessage(MSG.color(prefix + "&cThe reported clan does not exist."));
            return;
        }

        List<String> reports = dataConfig.getStringList("Clans." + reportedClan + ".Reports");

        if (reports.contains(reason)) {
            sender.sendMessage(MSG.color(prefix + "&cThis report has already been submitted."));
            return;
        }

        reports.add(reason);
        dataConfig.set("Clans." + reportedClan + ".Reports", reports);

        fh.saveData();

        sender.sendMessage(MSG.color(prefix + "&2Clan reported: &e" + reportedClan + ". Reason: " + reason));
    }

    public void disband(CommandSender sender, String playerClan) {
        if (playerClan == null || playerClan.isEmpty()) {
            sender.sendMessage(MSG.color(prefix + "&cYou are not in a clan."));
            return;
        }

        Player player = (Player) sender;
        FileHandler fh = plugin.getFH();
        FileConfiguration dataConfig = fh.getData();
        String leader = dataConfig.getString("Clans." + playerClan + ".Leader");

        if (!player.getName().equalsIgnoreCase(leader)) {
            sender.sendMessage(MSG.color(prefix + "&cYou are not the leader of this clan."));
            return;
        }

        if (!dataConfig.isConfigurationSection("Clans." + playerClan)) {
            sender.sendMessage(MSG.color(prefix + "&cError: Clan data not found."));
            return;
        }

        if (fh.getConfig().getBoolean("config.vault-integration.enabled")) {
            int deleteGain = fh.getConfig().getInt("config.vault-integration.earn.delete-clan");
            econ.depositPlayer(player, deleteGain);
            sender.sendMessage(MSG.color(prefix + "&2The clan was eliminated. You won: &e$" + deleteGain));
        } else {
            sender.sendMessage(MSG.color(prefix + "&2The clan was eliminated."));
        }

        dataConfig.set("Clans." + playerClan, null);
        fh.saveData();
    }

    public void create(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(MSG.color(prefix + "&c&lUSE:&f /cls create &5(name of your clan)."));
            return;
        }

        Player player = (Player) sender;
        String clanName = args[1].toLowerCase();
        FileHandler fh = plugin.getFH();
        FileConfiguration data = fh.getData();

        if (fh.getConfig().getStringList("names-blocked.blocked").contains(clanName) || data.isConfigurationSection("Clans." + clanName)) {
            sender.sendMessage(MSG.color(prefix + "&cThis name is not allowed or already exists."));
            return;
        }

        if (fh.getConfig().getBoolean("vault-integration.enabled")) {
            int createCost = fh.getConfig().getInt("vault-integration.cost.create-clan");
            if (econ.getBalance(player) < createCost) {
                sender.sendMessage(MSG.color("&cYou don’t have enough money. You need: &2&l$" + createCost));
                return;
            }
            econ.withdrawPlayer(player, createCost);
        }

        String playerName = player.getName();
        data.set("Clans." + clanName + ".Founder", playerName);
        data.set("Clans." + clanName + ".Leader", playerName);
        data.set("Clans." + clanName + ".Users", Collections.singletonList(playerName));
        fh.saveData();

        player.sendMessage(MSG.color(prefix + "&2Your clan &e" + clanName + " &2has been created."));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            return args.length == 1 ? List.of("reload") : new ArrayList<>();
        }

        Player player = (Player) sender;
        String playerClan = getPlayerClan(player.getName());

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(List.of("create", "disband", "report", "list", "join", "war", "kick", "invite", "chat", "leave", "stats", "resign"));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "join":
                    if (isNotInClan(playerClan)) {
                        completions.addAll(getClanNames());
                    }
                    break;
                case "invite":
                case "kick":
                    if (isInClan(playerClan) && isLeader(player, playerClan)) {
                        completions.addAll(getOnlinePlayerNames());
                    }
                    break;
                case "war":
                    completions.addAll(List.of("declare", "peace", "alliance", "accept", "deny"));
                    break;
                case "report":
                    completions.addAll(getClanNames());
                    break;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("war")) {
                switch (args[1].toLowerCase()) {
                    case "declare":
                    case "peace":
                    case "alliance":
                    case "accept":
                    case "deny":
                        if (isInClan(playerClan) && isLeader(player, playerClan)) {
                            completions.addAll(getClanNames());
                        }
                        break;
                }
            }
        }

        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }

    private boolean isInClan(String clan) {
        return clan != null && !clan.isEmpty();
    }

    private boolean isNotInClan(String clan) {
        return !isInClan(clan);
    }

    private boolean isLeader(Player player, String clanName) {
        FileHandler fh = plugin.getFH();
        FileConfiguration dataConfig = fh.getData();
        String leader = dataConfig.getString("Clans." + clanName + ".Leader");
        return player.getName().equalsIgnoreCase(leader);
    }

    private List<String> getClanNames() {
        FileHandler fh = plugin.getFH();
        FileConfiguration dataConfig = fh.getData();
        return new ArrayList<>(Objects.requireNonNull(dataConfig.getConfigurationSection("Clans")).getKeys(false));
    }

    private List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }
}