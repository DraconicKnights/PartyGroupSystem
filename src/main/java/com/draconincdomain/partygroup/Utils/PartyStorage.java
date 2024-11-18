package com.draconincdomain.partygroup.Utils;

import com.draconincdomain.partygroup.Objects.Party;

import java.util.Map;

public interface PartyStorage {
    void saveParty(Party party);
    Party loadParty();
    void removeParty(Party party);
    Map<Integer, Party> loadAllParties();
}
