package com.draconincdomain.partygroup.Managers;

import com.draconincdomain.partygroup.Objects.LocalDataTimeAdapter;
import com.draconincdomain.partygroup.Objects.Party;
import com.draconincdomain.partygroup.PartyGroup;
import com.draconincdomain.partygroup.Utils.PartyStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Party Data Manager
 * Used for storing party related data
 * Currently under work
 */
public class PartyDataManager implements PartyStorage {

    private static PartyDataManager instance;
    private static final Path PLUGIN_FOLDER = PartyGroup.getInstance().getPluginDataDirectory();
    private static final Path PARTY_FOLDER = PLUGIN_FOLDER.resolve("PartyData");
    private static final Path PARTY_FILE = PARTY_FOLDER.resolve("parties.json");

    private final Gson gson;
    private final Map<Integer, Party> parties;

    public PartyDataManager() {
        instance = this;
        gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDataTimeAdapter()).create();
        parties = loadAllParties();
        PartyManager.getInstance().getAllParties().putAll(parties);
    }

    @Override
    public void saveParty(Party party) {
        parties.put(party.getPartyId(), party);
    }

    @Override
    public Party loadParty() {
        return null;
    }

    @Override
    public void removeParty(Party party) {
        parties.remove(party.getPartyId());
        saveAllParties();
    }

    @Override
    public Map<Integer, Party> loadAllParties() {
        createDirectoriesIfNeeded();

        if (!Files.exists(PARTY_FILE)) {
            return new HashMap<>();
        }

        try (FileReader reader = new FileReader(PARTY_FILE.toFile())) {
            Type type = new TypeToken<Map<Integer, Party>>() {}.getType();
            Map<Integer, Party> loadedParties = gson.fromJson(reader, type);
            return loadedParties != null ? loadedParties : new HashMap<>();
        } catch (IOException e) {
            PartyGroup.getInstance().getLogger().error("Failed to load party data", e);
            return new HashMap<>();
        }
    }

    public void saveAllParties() {
        createDirectoriesIfNeeded();

        try (FileWriter writer = new FileWriter(PARTY_FILE.toFile())) {
            gson.toJson(parties, writer);
        } catch (IOException e) {
            PartyGroup.getInstance().getLogger().error("Failed to save party data", e);
        }
    }

    private void createDirectoriesIfNeeded() {
        try {
            if (!Files.exists(PLUGIN_FOLDER)) {
                Files.createDirectories(PLUGIN_FOLDER);
            }
            if (!Files.exists(PARTY_FOLDER)) {
                Files.createDirectories(PARTY_FOLDER);
            }
        } catch (IOException e) {
            PartyGroup.getInstance().getLogger().error("Failed to create directories", e);
        }
    }

    public Map<Integer, Party> getParties() {
        return parties;
    }

    public static PartyDataManager getInstance() {
        return instance;
    }
}
