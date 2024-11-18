package com.draconincdomain.partygroup.Commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public abstract class CommandCore implements SimpleCommand {
    protected String commandName;
    protected String description;
    protected String usage;
    protected String[] aliases;
    protected String permission;
    protected boolean requiresPlayer;
    protected boolean hasCooldown;
    protected int cooldownDuration;
    protected Map<UUID, Long> cooldowns = new HashMap<>();

    public void register(String commandName, String description, String usage, String[] aliases, String permission, boolean requiresPlayer, boolean hasCooldown, int cooldownDuration) {
        this.commandName = commandName;
        this.description = description;
        this.usage = usage;
        this.aliases = aliases;
        this.permission = permission;
        this.requiresPlayer = requiresPlayer;
        this.hasCooldown = hasCooldown;
        this.cooldownDuration = cooldownDuration;
    }

    protected abstract void execute(Player player, String[] args);

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (requiresPlayer && !(source instanceof Player)) {
            source.sendMessage(Component.text("This command can only be used by players."));
            return;
        }

        if (source instanceof Player player) {
            if (!player.hasPermission(permission)) {
                player.sendMessage(Component.text("You don't have permission to use this command."));
                return;
            }

            // Check cooldown
            if (hasCooldown && isOnCooldown(player)) {
                long remainingTime = getRemainingCooldownTime(player);
                player.sendMessage(Component.text("You are on cooldown for " + remainingTime + " seconds."));
                return;
            }

            execute(player, args);

            if (hasCooldown) {
                setCooldown(player);
            }
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return Collections.emptyList();
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.completedFuture(suggest(invocation));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        CommandSource source = invocation.source();
        return source.hasPermission(permission);
    }

    private boolean isOnCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId)) return false;
        long lastUse = cooldowns.get(playerId);
        return (System.currentTimeMillis() - lastUse) / 1000 < cooldownDuration;
    }

    private void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    private long getRemainingCooldownTime(Player player) {
        long lastUse = cooldowns.get(player.getUniqueId());
        return cooldownDuration - ((System.currentTimeMillis() - lastUse) / 1000);
    }
}
