package com.draconincdomain.partygroup;

import com.draconincdomain.partygroup.Annotations.Command;
import com.draconincdomain.partygroup.Annotations.Event;
import com.draconincdomain.partygroup.Commands.CommandCore;
import com.draconincdomain.partygroup.Managers.PartyDataManager;
import com.draconincdomain.partygroup.Managers.PartyManager;
import com.draconincdomain.partygroup.Objects.Party;
import com.draconincdomain.partygroup.Utils.ColourUtil;
import com.draconincdomain.partygroup.Utils.ComponentBuilder;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Party Group Plugin
 * Plugin Entry point
 * Used for creating player parties within a velocity network
 */
@Plugin(id = "velocity-partygroup", name = "Velocity-PartyGroup", version = BuildConstants.VERSION)
public class PartyGroup {
    private final ProxyServer server;
    private final Path pluginDataDirectory;
    private final Logger logger;
    private static PartyGroup instance;

    @Inject
    public PartyGroup(ProxyServer server, Logger logger, @DataDirectory Path pluginDataDirectory) {
        this.server = server;
        this.logger = logger;
        this.pluginDataDirectory = pluginDataDirectory;
        instance = this;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        CommandManager commandManager = server.getCommandManager();
        registerPluginCommand(commandManager);
        registerPluginEvents();
        new PartyManager();
        new PartyDataManager();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        for (Party party : PartyManager.getInstance().getAllParties().values()) {
            party.savePartyData();
        }
        PartyDataManager.getInstance().saveAllParties();
    }

    public void registerPluginCommand(CommandManager commandManager) {
        String packageName = getClass().getPackage().getName();
        Reflections reflections = new Reflections(packageName, Scanners.TypesAnnotated);

        Set<Class<?>> customCommandClasses = reflections.getTypesAnnotatedWith(Command.class);

        for (Class<?> commandClass : customCommandClasses) {
            try {
                Command commandAnnotation = commandClass.getAnnotation(Command.class);
                String commandName = commandAnnotation.name();
                String description = commandAnnotation.description();
                String usage = commandAnnotation.usage();
                String[] aliases = commandAnnotation.aliases();
                String permission = commandAnnotation.permission();
                boolean requiresPlayer = commandAnnotation.requiresPlayer();
                boolean hasCooldown = commandAnnotation.hasCooldown();
                int cooldownDuration = commandAnnotation.cooldownDuration();

                logger.info("Registering command: {}", commandName);

                CommandCore commandInstance = (CommandCore) commandClass.getDeclaredConstructor().newInstance();
                commandInstance.register(commandName, description, usage, aliases, permission, requiresPlayer, hasCooldown, cooldownDuration);

                commandManager.register(commandManager.metaBuilder(commandName).aliases(aliases).plugin(this).build(), commandInstance);

            } catch (Exception e) {
                logger.error("Failed to register command: {}", commandClass.getName(), e);
            }
        }
    }

    private void registerPluginEvents() {
        String packageName = getClass().getPackage().getName();
        Reflections reflections = new Reflections(packageName, Scanners.TypesAnnotated);

        Set<Class<?>> customEventClasses = reflections.getTypesAnnotatedWith(Event.class);

        for (Class<?> eventClass : customEventClasses) {
            try {
                Event eventAnnotation = eventClass.getAnnotation(Event.class);
                String name = eventAnnotation.name();

                logger.info("Registering event: {}", name);
                getServer().getEventManager().register(this, eventClass.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                logger.error("Failed to register event: {}", eventClass.getName(), e);
            }
        }
    }

    public Collection<Player> getOnlinePlayers() {
        return server.getAllPlayers();
    }

    public Optional<Player> getPlayerByUUID(UUID uuid) {
        return server.getPlayer(uuid);
    }

    public Optional<Player> getPlayerByName(String name) {
        return server.getPlayer(name);
    }

    public void broadcastMessage(String message, ColourUtil.CustomColour colour) {
        server.sendMessage(ComponentBuilder.create(message, ColourUtil.fromEnum(colour)).build());
    }

    public void sendMessage(UUID uuid, String message, ColourUtil.CustomColour colour) {
        Optional<Player> player = server.getPlayer(uuid);
        player.ifPresent(plyr -> plyr.sendMessage(ComponentBuilder.create(message, ColourUtil.fromEnum(colour)).build()));
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getPluginDataDirectory() {
        return pluginDataDirectory;
    }

    public static PartyGroup getInstance() {
        return instance;
    }
}
