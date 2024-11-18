package com.draconincdomain.partygroup.Objects;

import java.util.UUID;

/**
 * Party Invitation Request
 * Used for a set object of player and timed data that wil expire if not accepted within the set time frame
 */
public class InvitationRequest {
    private final UUID inviter;
    private final UUID invitee;
    private final Party party;
    private final long timeStamp;

    public InvitationRequest(UUID inviter, UUID invitee, Party party) {
        this.inviter = inviter;
        this.invitee = invitee;
        this.party = party;
        this.timeStamp = System.currentTimeMillis();
    }

    public UUID getInviter() {
        return inviter;
    }

    public UUID getInvitee() {
        return invitee;
    }

    public Party getParty() {
        return party;
    }

    public boolean isExpired() {
        return (System.currentTimeMillis() - timeStamp) > 60000;
    }
}
