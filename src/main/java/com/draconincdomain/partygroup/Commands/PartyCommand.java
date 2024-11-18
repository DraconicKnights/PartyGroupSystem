package com.draconincdomain.partygroup.Commands;

import com.draconincdomain.partygroup.Annotations.Command;
import com.draconincdomain.partygroup.Enums.PartyRoles;
import com.draconincdomain.partygroup.Managers.PartyManager;
import com.draconincdomain.partygroup.Objects.Party;
import com.draconincdomain.partygroup.PartyGroup;
import com.draconincdomain.partygroup.Utils.ColourUtil;
import com.draconincdomain.partygroup.Utils.ComponentBuilder;
import com.draconincdomain.partygroup.Utils.PlayerMessage;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import net.kyori.adventure.text.Component;

import java.util.Arrays;
import java.util.UUID;

/**
 * Party Command for Velocity
 * Default party command accessible to all players that have the party.default perm node
 */
@Command(name = "party", description = "Party command for players", usage = "/party", aliases = "p", permission = "party.default", hasCooldown = true, cooldownDuration = 2)
public class PartyCommand extends CommandCore {
    @Override
    protected void execute(Player player, String[] args) {
        if (args.length == 0) {
            displayPartyCommands(player);
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "create":
                createParty(player, args);
                break;
            case "invite":
                invitePlayer(player, args);
                break;
            case "leave":
                leaveParty(player);
                break;
            case "list":
                listParty(player);
                break;
            case "promote":
                promoteMember(player, args);
                break;
            case "demote":
                demoteMember(player, args);
                break;
            case "chat":
                handlePlayerChat(player, args);
                break;
            case "join":
                handleJoinParty(player);
                break;
            case "warp":
                warpParty(player);
                break;
            case "tp":
                if (args.length < 2) {
                    PlayerMessage.playerSendMessage(player, "Usage: /party tp <Player>", ColourUtil.CustomColour.RED);
                    return;
                }
                teleportToPlayer(player, args);
                break;
            case "disband":
                disbandParty(player);
                break;
            default:
                break;
        }
    }

    private void displayPartyCommands(Player player) {
        ComponentBuilder builder = ComponentBuilder.create("==========[ Party Commands ]===========\n", ColourUtil.CustomColour.GOLD.getTextColour());

        builder = builder
                .append("/party create ", ColourUtil.CustomColour.AQUA.getTextColour())
                .append("- Create a new party\n", ColourUtil.CustomColour.GOLD.getTextColour())
                .append("/party invite <playerName> ", ColourUtil.CustomColour.AQUA.getTextColour())
                .append("- Invite a player to your party\n", ColourUtil.CustomColour.GOLD.getTextColour())
                .append("/party leave ", ColourUtil.CustomColour.AQUA.getTextColour())
                .append("- Leave your current party\n", ColourUtil.CustomColour.GOLD.getTextColour())
                .append("/party list ", ColourUtil.CustomColour.AQUA.getTextColour())
                .append("- List all members in your party\n", ColourUtil.CustomColour.GOLD.getTextColour())
                .append("/party promote <playerName> ", ColourUtil.CustomColour.AQUA.getTextColour())
                .append("- Promote a member to party leader\n", ColourUtil.CustomColour.GOLD.getTextColour())
                .append("/party disband ", ColourUtil.CustomColour.AQUA.getTextColour())
                .append("- Disband your party\n", ColourUtil.CustomColour.GOLD.getTextColour())
                .append("/party chat ", ColourUtil.CustomColour.AQUA.getTextColour())
                .append("- Chat with your party members\n", ColourUtil.CustomColour.GOLD.getTextColour())
                .append("/party join ", ColourUtil.CustomColour.AQUA.getTextColour())
                .append("- Join a party\n", ColourUtil.CustomColour.GOLD.getTextColour())
                .append("/party warp ", ColourUtil.CustomColour.AQUA.getTextColour())
                .append("- Warps all party members to the same server\n", ColourUtil.CustomColour.GOLD.getTextColour())
                .append("/party tp ", ColourUtil.CustomColour.AQUA.getTextColour())
                .append("- Teleports you to a specific party member\n", ColourUtil.CustomColour.GOLD.getTextColour())
                .append("====================================", ColourUtil.CustomColour.GOLD.getTextColour());

        PlayerMessage.playerSendMessage(player, builder.build());
    }

    private void teleportToPlayer(Player player, String[] args) {
        if (args.length < 2) {
            PlayerMessage.playerSendMessage(player, "Usage: /party tp <Player>", ColourUtil.CustomColour.RED);
            return;
        }

        Player target = PartyGroup.getInstance().getServer().getPlayer(args[1]).orElse(null);
        if (target == null) {
            PlayerMessage.playerSendMessage(player, "Player not found", ColourUtil.CustomColour.RED);
            return;
        }

        Party party = PartyManager.getInstance().findPlayerParty(player.getUniqueId());

        if (!party.getPlayers().containsKey(target.getUniqueId())) {
            PlayerMessage.playerSendMessage(player, "Player is not in your party", ColourUtil.CustomColour.RED);
            return;
        }

        target.getCurrentServer().ifPresent(targetServer -> {
            player.getCurrentServer().ifPresent(playerServer -> {
                if (playerServer.getServer().equals(targetServer.getServer())) {
                    PlayerMessage.playerSendMessage(player, "Teleporting you to " + target.getUsername(), ColourUtil.CustomColour.GREEN);
                } else {
                    player.createConnectionRequest(target.getCurrentServer().get().getServer()).fireAndForget();
                    PlayerMessage.playerSendMessage(player, "Teleporting you to " + target.getUsername(), ColourUtil.CustomColour.GREEN);
                }
            });
        });
    }

    private void warpParty(Player player) {
        Party party = PartyManager.getInstance().findPlayerParty(player.getUniqueId());

        if (party == null || !party.isPartyLeader(player.getUniqueId())) {
            player.sendMessage(Component.text("You must be the party leader to use this command.", ColourUtil.CustomColour.RED.getTextColour()));
            return;
        }

        ServerConnection leaderServer = player.getCurrentServer().orElse(null);

        if (leaderServer == null) {
            player.sendMessage(Component.text("You are not connected to any server.", ColourUtil.CustomColour.RED.getTextColour()));
            return;
        }

        party.getPlayers().forEach((memberUUID, role) -> {
            Player member = PartyGroup.getInstance().getServer().getPlayer(memberUUID).orElse(null);

            if (member != null && !member.equals(player)) {
                member.createConnectionRequest(leaderServer.getServer()).fireAndForget();
                member.sendMessage(Component.text("You have been warped to the party leader's server.", ColourUtil.CustomColour.GREEN.getTextColour()));
            }
        });

        player.sendMessage(Component.text("Warping all party members to your server.", ColourUtil.CustomColour.GREEN.getTextColour()));
    }

    private void createParty(Player player, String[] args) {
        Party existingParty = PartyManager.getInstance().findPlayerParty(player.getUniqueId());

        if (existingParty != null) {
            PlayerMessage.playerSendMessage(player, "You are already in a party", ColourUtil.CustomColour.RED);
            return;
        }

        Party newParty = PartyManager.getInstance().createParty(player);
        PlayerMessage.playerSendMessage(player, "Party has successfully been created: " + newParty.getName(), ColourUtil.CustomColour.LIGHT_PURPLE);
    }

    private void disbandParty(Player player) {
        Party party = PartyManager.getInstance().findPlayerParty(player.getUniqueId());
        if (party == null) {
            PlayerMessage.playerSendMessage(player, "Party does not exist", ColourUtil.CustomColour.AQUA);
            return;
        }

        if (!party.isPartyLeader(player.getUniqueId())) {
            PlayerMessage.playerSendMessage(player, "Only the party leader can disband the party", ColourUtil.CustomColour.AQUA);
            return;
        }

        party.partyDisband();
    }

    private void invitePlayer(Player player, String[] args) {
        if (args.length < 2) {
            PlayerMessage.playerSendMessage(player, "Usage: /party invite <Player>", ColourUtil.CustomColour.RED);
            return;
        }

        Party party = PartyManager.getInstance().findPlayerParty(player.getUniqueId());
        if (party == null) {
            PlayerMessage.playerSendMessage(player, "Party does not exist", ColourUtil.CustomColour.AQUA);
            return;
        }

        if (!party.getLeader().equals(player.getUniqueId())) {
            PlayerMessage.playerSendMessage(player, "Only the party leader can invite players", ColourUtil.CustomColour.AQUA);
            return;
        }

        Player target = PartyGroup.getInstance().getServer().getPlayer(args[1]).orElse(null);
        if (target == null) {
            PlayerMessage.playerSendMessage(player, "Target player does not exist", ColourUtil.CustomColour.AQUA);
            return;
        }

        if (PartyManager.getInstance().isPlayerInParty(target.getUniqueId())) {
            PlayerMessage.playerSendMessage(player, "Player is already in a party", ColourUtil.CustomColour.AQUA);
            return;
        }

        PlayerMessage.playerSendMessage(player, "Player has been invited to the party", ColourUtil.CustomColour.AQUA);
        PartyManager.getInstance().invitePlayer(party, target);
    }

    private void leaveParty(Player player) {
        Party party = PartyManager.getInstance().findPlayerParty(player.getUniqueId());

        if (party == null) {
            PlayerMessage.playerSendMessage(player, "Party does not exist", ColourUtil.CustomColour.AQUA);
            return;
        }

        PartyManager.getInstance().removePlayerFromParty(party, player.getUniqueId());
    }

    private void listParty(Player player) {
        Party party = PartyManager.getInstance().findPlayerParty(player.getUniqueId());
        if (party == null) {
            PlayerMessage.playerSendMessage(player, "You are not in a party", ColourUtil.CustomColour.AQUA);
            return;
        }

        ComponentBuilder builder = ComponentBuilder.create("==========[ Party Info ]===========\n", ColourUtil.CustomColour.GOLD.getTextColour());

        builder.append("Party Name: ", ColourUtil.CustomColour.AQUA.getTextColour())
                .append(party.getName() + "\n", ColourUtil.CustomColour.DARK_AQUA.getTextColour());

        Player leader = PartyGroup.getInstance().getServer().getPlayer(party.getLeader()).orElse(null);
        if (leader != null) {
            builder.append("Party Lead: ", ColourUtil.CustomColour.YELLOW.getTextColour())
                    .append(leader.getUsername() + "\n", ColourUtil.CustomColour.LIGHT_PURPLE.getTextColour());
        }

        builder.append("==========[ Party Members ]===========\n", ColourUtil.CustomColour.AQUA.getTextColour());
        for (UUID memberUUID : party.getPlayers().keySet()) {
            Player member = PartyGroup.getInstance().getServer().getPlayer(memberUUID).orElse(null);
            if (member != null) {
                if (memberUUID.equals(party.getLeader())) {
                    builder.append(member.getUsername() + " - ", ColourUtil.CustomColour.DARK_AQUA.getTextColour())
                            .append(party.getRole(memberUUID).toString() + "\n", ColourUtil.CustomColour.GOLD.getTextColour());
                } else {
                    builder.append(member.getUsername() + " - ", ColourUtil.CustomColour.GREEN.getTextColour())
                            .append(party.getRole(memberUUID).toString() + "\n", ColourUtil.CustomColour.GREEN.getTextColour());
                }
            }
        }

        builder.append("====================================", ColourUtil.CustomColour.GOLD.getTextColour());
        PlayerMessage.playerSendMessage(player, builder.build());
    }

    private void promoteMember(Player player, String[] args) {
        if (args.length < 2) {
            PlayerMessage.playerSendMessage(player, "Usage: /party promote <Player>", ColourUtil.CustomColour.RED);
            return;
        }

        Party party = PartyManager.getInstance().findPlayerParty(player.getUniqueId());
        if (party == null) {
            PlayerMessage.playerSendMessage(player, "Party does not exist", ColourUtil.CustomColour.AQUA);
            return;
        }

        if (!party.isPartyLeader(player.getUniqueId())) {
            PlayerMessage.playerSendMessage(player, "Only the party leader can promote members", ColourUtil.CustomColour.AQUA);
            return;
        }

        Player target = PartyGroup.getInstance().getServer().getPlayer(args[1]).orElse(null);
        if (target == null || !party.getPlayers().containsKey(target.getUniqueId())) {
            PlayerMessage.playerSendMessage(player, "Player does not exist in the party", ColourUtil.CustomColour.AQUA);
            return;
        }

        PartyRoles currentRole = party.getRole(target.getUniqueId());
        if (currentRole == PartyRoles.LEADER) {
            PlayerMessage.playerSendMessage(player, "You can't promote the party leader", ColourUtil.CustomColour.AQUA);
            return;
        }

        if (currentRole == PartyRoles.MEMBER) {
            PlayerMessage.playerSendMessage(player, "Player: " + target.getUsername() + " has been promoted to Officer", ColourUtil.CustomColour.AQUA);
            party.promotePlayer(target.getUniqueId(), PartyRoles.OFFICER);
        } else {
            party.setLeader(target.getUniqueId());
            PlayerMessage.playerSendMessage(player, "Player: " + target.getUsername() + " has been promoted to Party Leader", ColourUtil.CustomColour.AQUA);
        }
    }

    private void demoteMember(Player player, String[] args) {
        if (args.length < 2) {
            PlayerMessage.playerSendMessage(player, "Usage: /party demote <Player>", ColourUtil.CustomColour.RED);
            return;
        }

        Party party = PartyManager.getInstance().findPlayerParty(player.getUniqueId());
        if (party == null) {
            PlayerMessage.playerSendMessage(player, "Party does not exist", ColourUtil.CustomColour.AQUA);
            return;
        }

        if (!party.isPartyLeader(player.getUniqueId())) {
            PlayerMessage.playerSendMessage(player, "Only the party leader can demote members", ColourUtil.CustomColour.AQUA);
            return;
        }

        Player target = PartyGroup.getInstance().getServer().getPlayer(args[1]).orElse(null);
        if (target == null || !party.getPlayers().containsKey(target.getUniqueId())) {
            PlayerMessage.playerSendMessage(player, "Player does not exist in the party", ColourUtil.CustomColour.AQUA);
            return;
        }

        PartyRoles currentRole = party.getRole(target.getUniqueId());
        if (currentRole == PartyRoles.OFFICER) {
            PlayerMessage.playerSendMessage(player, "Player: " + target.getUsername() + " has been demoted to Member", ColourUtil.CustomColour.AQUA);
            party.demotePlayer(target.getUniqueId(), PartyRoles.MEMBER);
        }
    }

    private void handleJoinParty(Player player) {
        UUID playerId = player.getUniqueId();

        if (PartyManager.getInstance().getPendingInvitations().containsKey(playerId)) {
            PartyManager.getInstance().joinParty(player);
        }
    }

    private void handlePlayerChat(Player player, String[] args) {
        if (args.length < 2) {
            return;
        }

        Party party = PartyManager.getInstance().findPlayerParty(player.getUniqueId());
        if (party == null) {
            return;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        sendPartyChatMessage(player, party, message);
    }

    private void sendPartyChatMessage(Player sender, Party party, String message) {

        String serverName = sender.getCurrentServer()
                .map(server -> server.getServerInfo().getName().toUpperCase())
                .orElse("UNKNOWN");

        ComponentBuilder messageBuilder = ComponentBuilder.create("[" + serverName + "] ", ColourUtil.CustomColour.DARK_AQUA.getTextColour())
                .append("[Party] ", ColourUtil.CustomColour.AQUA.getTextColour())
                .append("[" + party.getRole(sender.getUniqueId()) + "] ", ColourUtil.CustomColour.GOLD.getTextColour())
                .append(sender.getUsername(), ColourUtil.CustomColour.AQUA.getTextColour())
                .append(": ", ColourUtil.CustomColour.LIGHT_PURPLE.getTextColour())
                .append(message, ColourUtil.CustomColour.GREEN.getTextColour());

        party.getPlayers().forEach((memberUUID, role) -> {
            Player member = PartyGroup.getInstance().getServer().getPlayer(memberUUID).orElse(null);
            if (member != null) {
                member.sendMessage(messageBuilder.build());
            }
        });
    }
}
