package com.draconincdomain.partygroup.Events;

import com.draconincdomain.partygroup.Annotations.Event;
import com.draconincdomain.partygroup.Enums.PartyNotificationAlert;
import com.draconincdomain.partygroup.Managers.PartyDataManager;
import com.draconincdomain.partygroup.Managers.PartyManager;
import com.draconincdomain.partygroup.Objects.Party;
import com.draconincdomain.partygroup.PartyGroup;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.scheduler.ScheduledTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Player Event Listener
 * This is a player event listener that will be used for listening on player join and quit events and run a check to set for the specified player to be removed from the party if they don't re-connect within a set timeframe
 */
@Event(name = "PlayerEventListener")
public class PlayerEventListener {

    private final Map<UUID, ScheduledTask> reconnectionTasks = new ConcurrentHashMap<>();

    @Subscribe
    public void onPlayerJoinEvent(LoginEvent event) {
        Player player = event.getPlayer();
        PartyGroup.getInstance().getLogger().info("Player: {} has connected to the server", player.getUsername());

        ScheduledTask task = reconnectionTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    @Subscribe
    public void onPlayerLeaveEvent(DisconnectEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (PartyManager.getInstance().isPlayerInParty(playerUUID)) {
            Party party = PartyManager.getInstance().findPlayerParty(playerUUID);
            if (party != null) {
                PartyGroup.getInstance().getLogger().info("Player: {} has disconnected from the server", player.getUsername());

                PartyGroup.getInstance().getLogger().info("Player {} will be removed from {} party in 5 minuets", player.getUsername(), party.getName());

                PartyManager.getInstance().alertParty(party, PartyNotificationAlert.INFO, "Player: " + player.getUsername() + " has disconnected from the server and will be removed from the party in 5 minutes");

                ScheduledTask task = PartyGroup.getInstance().getServer().getScheduler().buildTask(PartyGroup.getInstance(), () -> {

                    PartyManager.getInstance().alertParty(party, PartyNotificationAlert.INFO, "Player: " + player.getUsername() + " has been removed from the party");

                    PartyManager.getInstance().removePlayerFromParty(party, playerUUID);
                    PartyGroup.getInstance().getLogger().info("Player: {} has been removed from the party after 5 minutes.", player.getUsername());

                    if (!party.getPlayers().isEmpty()) {
                        PartyDataManager.getInstance().saveParty(party);
                    }

                }).delay(5, TimeUnit.MINUTES).schedule();

                reconnectionTasks.put(playerUUID, task);
            }
        }
    }
}
