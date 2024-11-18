package com.draconincdomain.partygroup.Utils;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class PlayerMessage {

    public static void playerSendMessage(Player player, String messageContent, ColourUtil.CustomColour colour) {
        TextColor textColor = ColourUtil.fromEnum(colour);
        Component content = ComponentBuilder.create(messageContent,textColor).build();
        player.sendMessage(content);
    }

    public static void playerSendMessage(Player player, Component component) {
        player.sendMessage(component);
    }
}
