package com.draconincdomain.partygroup.Utils;

import com.draconincdomain.partygroup.PartyGroup;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.UUID;

public class PluginMessageBuilder {

    private final ByteArrayDataOutput output;

    public PluginMessageBuilder(String subChannel) {
        this.output = ByteStreams.newDataOutput();
        this.output.writeUTF(subChannel);
    }

    public PluginMessageBuilder writeString(String value) {
        this.output.writeUTF(value);
        return this;
    }

    public PluginMessageBuilder writeUUID(UUID uuid) {
        this.output.writeUTF(uuid.toString());
        return this;
    }

    public PluginMessageBuilder writeInt(int value) {
        this.output.writeInt(value);
        return this;
    }

    public PluginMessageBuilder writeBoolean(boolean value) {
        this.output.writeBoolean(value);
        return this;
    }

    public byte[] build() {
        return this.output.toByteArray();
    }

    public boolean send(RegisteredServer server) {
        return PartyGroup.getInstance().sendPluginMessage(server, this.build());
    }

    public boolean send(Player player) {
        return PartyGroup.getInstance().sendPluginMessage(player, this.build());
    }
}
