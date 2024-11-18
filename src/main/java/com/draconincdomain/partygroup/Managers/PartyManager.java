package com.draconincdomain.partygroup.Managers;

import com.draconincdomain.partygroup.Enums.PartyNotificationAlert;
import com.draconincdomain.partygroup.Objects.InvitationRequest;
import com.draconincdomain.partygroup.Objects.Party;
import com.draconincdomain.partygroup.PartyGroup;
import com.draconincdomain.partygroup.Utils.ColourUtil;
import com.draconincdomain.partygroup.Utils.ComponentBuilder;
import com.draconincdomain.partygroup.Utils.PlayerMessage;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Party Manager
 * A manager object used for storing and managing all party related tasks
 */
public class PartyManager {
    private static PartyManager Instance;
    private static Map<Integer, Party> allParties = new HashMap<>();
    private static Map<UUID, InvitationRequest> pendingInvitations = new HashMap<>();
    private static int nextPartyId = 1;

    public PartyManager() {
        Instance = this;
    }

    public Map<Integer, Party> getAllParties() {
        return allParties;
    }
    public Map<UUID, InvitationRequest> getPendingInvitations() {
        return pendingInvitations;
    }

    public Party createParty(Player player) {
        Party newParty = new Party(player.getUsername() + "'s Party", player.getUniqueId(), player.getUsername(), nextPartyId++, LocalDateTime.now());
        allParties.put(newParty.getPartyId(), newParty);
        return newParty;
    }

    public void invitePlayer(Party party, Player player) {
        InvitationRequest invitationRequest = new InvitationRequest(party.getLeader(), player.getUniqueId(), party);
        pendingInvitations.put(player.getUniqueId(), invitationRequest);

        Component message = ComponentBuilder.create("You have been invited to join: ", ColourUtil.fromEnum(ColourUtil.CustomColour.AQUA))
                .hover("[Click to join Party]")
                .click("/party join")
                .build();
        Component clickToJoin = ComponentBuilder.create("[Click to join]", ColourUtil.fromEnum(ColourUtil.CustomColour.AQUA))
                .hover("Click to join")
                .click("/party join")
                .build();

        player.sendMessage(message.append(clickToJoin));

        PartyGroup.getInstance().getServer().getScheduler().buildTask(PartyGroup.getInstance(), () -> {
            if (pendingInvitations.containsKey(player.getUniqueId())) {
                pendingInvitations.remove(player.getUniqueId());
                PlayerMessage.playerSendMessage(player, "Your invitation to join: " + party.getName() + " has expired", ColourUtil.CustomColour.RED);
                var sender = PartyGroup.getInstance().getServer().getPlayer(party.getLeader()).get();
                PlayerMessage.playerSendMessage(sender, "Player: " + player.getUsername() + " Has not accepted the invitation", ColourUtil.CustomColour.RED);
            }
        }).delay(60L, TimeUnit.SECONDS).schedule();
    }

    public void joinParty(Player player) {
        InvitationRequest invitationRequest = pendingInvitations.get(player.getUniqueId());

        if (invitationRequest != null && !invitationRequest.isExpired()) {
            Party party = invitationRequest.getParty();
            if (party != null) {
                addPlayerToParty(party, player.getUniqueId());
                pendingInvitations.remove(player.getUniqueId());
                PlayerMessage.playerSendMessage(player, "You have joined: " + party.getName(), ColourUtil.CustomColour.GREEN);
                alertParty(party, PartyNotificationAlert.INFO, player.getUsername() + " Has joined the party");
            } else {
                PlayerMessage.playerSendMessage(player, "This party no longer exists", ColourUtil.CustomColour.RED);
            }
        } else {
            PlayerMessage.playerSendMessage(player, "Your invitation to this party has expired or no longer exists", ColourUtil.CustomColour.RED);
        }
    }

    public void acceptInvitation(Player leader, UUID invitee) {
        if (pendingInvitations.containsKey(invitee)) {
            InvitationRequest invitationRequest = pendingInvitations.get(invitee);
            if (invitationRequest.getInviter().equals(leader.getUniqueId())) {
                joinParty(PartyGroup.getInstance().getServer().getPlayer(invitee).get());
            }
        }
    }

    public void removePlayerFromParty(Party party, UUID playerUUID) {
/*        Map<PartyMap, Party> activeInstances = MapManager.getInstance().getActiveInstances();
        PartyMap partyMap = MapManager.getInstance().getActivePartyMapInstance(party);
        Player player = Bukkit.getPlayer(playerUUID);

        if (player != null) {
            if (activeInstances.containsKey(partyMap)) {
                Party activeParty = activeInstances.get(partyMap);

                if (activeParty != null && activeParty.getPlayers().containsKey(playerUUID)) {
                    World mainWorld = Bukkit.getWorld("world");
                    if (mainWorld != null) {
                        player.teleport(mainWorld.getSpawnLocation());
                        player.sendMessage(ChatColor.YELLOW + "You have been teleported back to the main world.");
                    } else {
                        player.sendMessage(ChatColor.RED + "Main world not found.");
                    }
                }
            }

        }*/
        party.removeMember(playerUUID);

        /*if (party.getPlayers().isEmpty()) {
            if (partyMap != null) {
                MapManager.getInstance().cleanupMapInstance(partyMap);
                Bukkit.getLogger().info("Party is empty. Map instance for " + partyMap.getName() + " has been unloaded and cleaned up.");
            }
        }*/
    }

    public void addPlayerToParty(Party party, UUID playerUUID) {
        party.addMember(playerUUID);
    }

    public void disbandParty(Party party) {
        allParties.remove(party.getPartyId());
    }

    public void alertParty(Party party, PartyNotificationAlert alert, String message) {
        party.getPlayers().forEach((playerUUID, role) -> {
            PartyGroup.getInstance().getServer().getPlayer(playerUUID).ifPresent(player -> {

                Component componentMessage = switch (alert) {
                    case INFO -> ComponentBuilder.create(message, ColourUtil.fromEnum(ColourUtil.CustomColour.GREEN)).build();
                    case WARNING -> ComponentBuilder.create(message, ColourUtil.fromEnum(ColourUtil.CustomColour.RED)).build();
                    case SERVER -> ComponentBuilder.create(message, ColourUtil.fromEnum(ColourUtil.CustomColour.BLUE)).build();
                };

                player.sendMessage(componentMessage);
            });
        });
    }

    public Party findPlayerParty(UUID playerUUID) {
        for (Party party : getAllParties().values()) {
            if (party.getPlayers().containsKey(playerUUID)) {
                return party;
            }
        }
        return null;
    }

    public boolean isPlayerInParty(UUID playerUUID) {
        for (Party party : getAllParties().values()) {
            if (party.getPlayers().containsKey(playerUUID))
                return true;
        }
        return false;
    }
    public static PartyManager getInstance() {
        return Instance;
    }
}
