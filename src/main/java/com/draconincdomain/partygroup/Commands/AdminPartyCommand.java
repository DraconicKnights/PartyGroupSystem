package com.draconincdomain.partygroup.Commands;

import com.draconincdomain.partygroup.Annotations.Command;
import com.draconincdomain.partygroup.Managers.PartyManager;
import com.draconincdomain.partygroup.Objects.Party;
import com.draconincdomain.partygroup.PartyGroup;
import com.draconincdomain.partygroup.Utils.ColourUtil;
import com.draconincdomain.partygroup.Utils.ComponentBuilder;
import com.draconincdomain.partygroup.Utils.PlayerMessage;
import com.velocitypowered.api.proxy.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Admin Party Command
 * An Admin party command that can be used by players with the party.admin perm node
 * Primarily should be used for moderating and keeping a check on player parties
 */
@Command(name = "aparty", description = "Admin Party command", usage = "/aparty", aliases = {"ap"}, permission = "party.admin")
public class AdminPartyCommand extends CommandCore {

    @Override
    protected void execute(Player player, String[] args) {

        if (args.length == 0) {
            displayPartyCommands(player);
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "list":
                displayPartyList(player);
                break;
            case "details":
                if (args.length < 2) {
                    PlayerMessage.playerSendMessage(player, "Usage: /aparty details <partyId>", ColourUtil.CustomColour.RED);
                    return;
                }
                displayPartyDetails(player, args[1]);
                break;
            case "forcejoin":
                if (args.length < 2) {
                    PlayerMessage.playerSendMessage(player, "Usage: /aparty forcejoin <partyId>", ColourUtil.CustomColour.RED);
                    return;
                }
                forceJoinParty(player, args[1], args[2]);
                break;
            case "forceleave":
                if (args.length < 2) {
                    PlayerMessage.playerSendMessage(player, "Usage: /aparty forceleave <partyId>", ColourUtil.CustomColour.RED);
                    return;
                }
                forceLeaveParty(player, args[1]);
                break;
            case "disband":
                if (args.length < 2) {
                    PlayerMessage.playerSendMessage(player, "Usage: /aparty disband <partyId>", ColourUtil.CustomColour.RED);
                    return;
                }
                disbandParty(player, args[1]);
                break;
            default:
                PlayerMessage.playerSendMessage(player, "Invalid aparty command.", ColourUtil.CustomColour.RED);
                break;
        }
    }

    private void displayPartyCommands(Player player) {
        ComponentBuilder builder = ComponentBuilder.create("==========[ Admin Party Commands ]===========\n", ColourUtil.CustomColour.GOLD.getTextColour());

        builder = builder
                .append("/aparty list", ColourUtil.CustomColour.AQUA.getTextColour())
                .append(" - List all parties\n", ColourUtil.CustomColour.GOLD.getTextColour())
                .append("/aparty details <partyID>", ColourUtil.CustomColour.AQUA.getTextColour())
                .append(" - Gives details of the target party\n", ColourUtil.CustomColour.GOLD.getTextColour())
                .append("/aparty forcejoin <partyID>", ColourUtil.CustomColour.AQUA.getTextColour())
                .append(" - Force join the target party\n", ColourUtil.CustomColour.GOLD.getTextColour())
                .append("/aparty forceleave <partyID>", ColourUtil.CustomColour.AQUA.getTextColour())
                .append(" - Force leave the target party\n", ColourUtil.CustomColour.GOLD.getTextColour())
                .append("/aparty disband <partyID>", ColourUtil.CustomColour.AQUA.getTextColour())
                .append(" - Force the party to disband\n", ColourUtil.CustomColour.GOLD.getTextColour())
                .append("====================================", ColourUtil.CustomColour.GOLD.getTextColour());

        PlayerMessage.playerSendMessage(player, builder.build());
    }

    private void displayPartyList(Player player) {
        ComponentBuilder builder = ComponentBuilder.create("==========[ Party List ]===========\n", ColourUtil.CustomColour.GOLD.getTextColour());

        Map<Integer, Party> allParties = PartyManager.getInstance().getAllParties();
        if (allParties.isEmpty()) {
            builder.append("No active parties at the moment.\n", ColourUtil.CustomColour.RED.getTextColour());
        } else {
            for (Party party : allParties.values()) {
                Optional<Player> leader = PartyGroup.getInstance().getServer().getPlayer(party.getLeader());
                builder.append("Party ID: ", ColourUtil.CustomColour.AQUA.getTextColour())
                        .append(String.valueOf(party.getPartyId()), ColourUtil.CustomColour.RED.getTextColour())
                        .append(" - Leader: ", ColourUtil.CustomColour.AQUA.getTextColour())
                        .append(leader.isPresent() ? leader.get().getUsername() : "Unknown", ColourUtil.CustomColour.GOLD.getTextColour())
                        .append("\n", ColourUtil.CustomColour.GOLD.getTextColour());
            }
        }

        builder.append("====================================", ColourUtil.CustomColour.GOLD.getTextColour());
        PlayerMessage.playerSendMessage(player, builder.build());
    }

    private void displayPartyDetails(Player player, String partyIdStr) {
        int partyId;
        try {
            partyId = Integer.parseInt(partyIdStr);
        } catch (NumberFormatException e) {
            PlayerMessage.playerSendMessage(player, "Invalid party ID.", ColourUtil.CustomColour.RED);
            return;
        }

        Party party = PartyManager.getInstance().getAllParties().get(partyId);
        if (party == null) {
            PlayerMessage.playerSendMessage(player, "Party not found.", ColourUtil.CustomColour.RED);
            return;
        }

        ComponentBuilder builder = ComponentBuilder.create("==========[ Party Details ]===========\n", ColourUtil.CustomColour.GOLD.getTextColour())
                .append("Founder: ", ColourUtil.CustomColour.DARK_PURPLE.getTextColour())
                .append(party.getFounderName() + "\n", ColourUtil.CustomColour.GOLD.getTextColour())
                .append("Founded Date: ", ColourUtil.CustomColour.YELLOW.getTextColour())
                .append(party.getFormattedDate() + "\n", ColourUtil.CustomColour.LIGHT_PURPLE.getTextColour())
                .append("==========[ Party Info ]===========\n", ColourUtil.CustomColour.GOLD.getTextColour())
                .append("Party Name: ", ColourUtil.CustomColour.AQUA.getTextColour())
                .append(party.getName() + "\n", ColourUtil.CustomColour.LIGHT_PURPLE.getTextColour());

        Optional<Player> leader = PartyGroup.getInstance().getServer().getPlayer(party.getLeader());
        leader.ifPresent(value -> builder.append("Party Leader: ", ColourUtil.CustomColour.YELLOW.getTextColour())
                .append(value.getUsername() + "\n", ColourUtil.CustomColour.LIGHT_PURPLE.getTextColour()));

        builder.append("==========[ Party Members ]===========\n", ColourUtil.CustomColour.GOLD.getTextColour());
        for (UUID memberUUID : party.getPlayers().keySet()) {
            Optional<Player> member = PartyGroup.getInstance().getServer().getPlayer(memberUUID);
            member.ifPresent(value -> builder.append(value.getUsername(), ColourUtil.CustomColour.DARK_PURPLE.getTextColour())
                    .append(" - ", ColourUtil.CustomColour.GOLD.getTextColour())
                    .append(party.getRole(memberUUID).toString() + "\n", ColourUtil.CustomColour.GOLD.getTextColour()));
        }

        builder.append("====================================", ColourUtil.CustomColour.GOLD.getTextColour());
        PlayerMessage.playerSendMessage(player, builder.build());
    }

    private void forceJoinParty(Player admin, String playerName, String partyId) {
        Player target = PartyGroup.getInstance().getServer().getPlayer(playerName).orElse(null);
        if (target == null) {
            PlayerMessage.playerSendMessage(admin, "Player not found", ColourUtil.CustomColour.RED);
            return;
        }

        Party party = PartyManager.getInstance().getAllParties().get(partyId);
        if (party == null) {
            PlayerMessage.playerSendMessage(admin, "Party not found", ColourUtil.CustomColour.RED);
            return;
        }

        party.addMember(target.getUniqueId());
        PlayerMessage.playerSendMessage(target, "You have been added to the party " + partyId, ColourUtil.CustomColour.GREEN);
        PlayerMessage.playerSendMessage(admin, "Player " + playerName + " has been added to the party " + partyId, ColourUtil.CustomColour.GREEN);
    }

    private void forceLeaveParty(Player admin, String playerName) {
        Player target = PartyGroup.getInstance().getServer().getPlayer(playerName).orElse(null);
        if (target == null) {
            PlayerMessage.playerSendMessage(admin, "Player not found", ColourUtil.CustomColour.RED);
            return;
        }

        if (!PartyManager.getInstance().isPlayerInParty(target.getUniqueId())) {
            PlayerMessage.playerSendMessage(admin, "Player is not in any party", ColourUtil.CustomColour.RED);
            return;
        }

        Party party = PartyManager.getInstance().getAllParties().get(target);
        party.removeMember(target.getUniqueId());
        PlayerMessage.playerSendMessage(target, "You have been forcefully removed from your party", ColourUtil.CustomColour.RED);
        PlayerMessage.playerSendMessage(admin, "Player " + playerName + " has been forcefully removed from their party", ColourUtil.CustomColour.GREEN);
    }

    private void disbandParty(Player admin, String partyId) {
        Party party = PartyManager.getInstance().getAllParties().get(partyId);
        if (party == null) {
            PlayerMessage.playerSendMessage(admin, "Party not found", ColourUtil.CustomColour.RED);
            return;
        }

        party.partyDisband();
        PlayerMessage.playerSendMessage(admin, "Party " + partyId + " has been disbanded", ColourUtil.CustomColour.GREEN);
    }
}