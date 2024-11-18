package com.draconincdomain.partygroup.Objects;

import com.draconincdomain.partygroup.Enums.PartyNotificationAlert;
import com.draconincdomain.partygroup.Enums.PartyRoles;
import com.draconincdomain.partygroup.Managers.PartyDataManager;
import com.draconincdomain.partygroup.Managers.PartyManager;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Party object
 * This is the party object that will be created for each party and the following data that will be stored
 */
public class Party implements Serializable {
    @SerializedName("name")
    private String Name;
    @SerializedName("leader")
    private UUID Leader;
    @SerializedName("founderName")
    private final String FounderName;
    @SerializedName("partyId")
    private final int partyId;
    @SerializedName("date")
    private LocalDateTime Date;
    @SerializedName("players")
    private final Map<UUID, PartyRoles> Players;

    public Party(String name, UUID leader,String founderName, int partyId, LocalDateTime date) {
        Name = name;
        this.Players = new HashMap<>();
        this.Leader = leader;
        this.FounderName = founderName;
        this.partyId = partyId;
        this.Date = date;
        this.Players.put(leader, PartyRoles.LEADER);
    }

    public String getName() {
        return Name;
    }

    public UUID getLeader() {
        return Leader;
    }

    public String getFounderName() {
        return FounderName;
    }

    public LocalDateTime getDate() {
        return Date;
    }

    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return Date.format(formatter);
    }

    public void setName(String name) {
        Name = name;
    }

    public void setLeader(UUID leader) {
        if (Players.containsKey(leader)) {
            this.Players.put(this.Leader, PartyRoles.MEMBER);
            this.Leader = leader;
            this.Players.put(leader, PartyRoles.LEADER);
        }
    }

    private void promoteNextLeader() {
        for (UUID memberUUID : Players.keySet()) {
            if (Players.get(memberUUID) == PartyRoles.OFFICER || Players.get(memberUUID) == PartyRoles.MEMBER) {
                setLeader(memberUUID);
                break;
            }
        }
    }

    public Map<UUID, PartyRoles> getPlayers() {
        return Players;
    }

    public void addMember(UUID playerUUID) {
        Players.put(playerUUID, PartyRoles.MEMBER);
    }

    public void removeMember(UUID playerUUID) {
        if (playerUUID.equals(Leader) && !Players.isEmpty()) {
            promoteNextLeader();
        }

        if (Players.isEmpty()) {
            PartyManager.getInstance().alertParty(this, PartyNotificationAlert.SERVER, "Their are no more members left within this party so it will be auto disbanded");
            partyDisband();
        }
        Players.remove(playerUUID);
    }

    public void partyDisband() {
        PartyManager.getInstance().alertParty(this, PartyNotificationAlert.INFO, "Your party is being disbanded");
        removePartyData();
        Players.clear();
        PartyManager.getInstance().disbandParty(this);
    }

    public PartyRoles getRole(UUID playerUUID) {
        return getPlayers().getOrDefault(playerUUID, PartyRoles.MEMBER);
    }

    public int getSize() {
        return this.getPlayers().size();
    }

    public int getPartyId() {
        return partyId;
    }

    public boolean isPartyLeader(UUID playerUUID) {
        return Leader.equals(playerUUID);
    }

    public void promotePlayer(UUID playerUUID, PartyRoles newRole) {
        if (getPlayers().containsKey(playerUUID)) {
            getPlayers().put(playerUUID, newRole);
        }
    }

    public void demotePlayer(UUID playerUUID, PartyRoles newRole) {
        if (getPlayers().containsKey(playerUUID)) {
            getPlayers().put(playerUUID, newRole);
        }
    }

    public void savePartyData() {
        PartyDataManager.getInstance().saveParty(this);
    }

    private void removePartyData() {
        PartyDataManager.getInstance().removeParty(this);
    }
}
