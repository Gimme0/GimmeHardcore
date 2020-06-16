package me.gimme.gimmehardcore.respawn;

import me.gimme.gimmehardcore.GimmeHardcore;
import me.gimme.gimmehardcore.listeners.AbstractEventListener;
import me.gimme.gimmehardcore.respawn.death.AwaitAction;
import me.gimme.gimmehardcore.respawn.death.DeathLock;
import me.gimme.gimmehardcore.respawn.death.DeathSpectating;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class RespawnDelay extends AbstractEventListener implements CommandExecutor {

    private int maxSpectatorDistance = plugin.getConfig().getInt(GimmeHardcore.CONFIG_RESPAWN_SPECTATOR_MAX_DISTANCE);

    private DeathLock deathLock = new DeathLock(plugin);
    private DeathSpectating deathSpectating = new DeathSpectating(plugin);
    private AwaitAction awaitAction = new AwaitAction(plugin);

    public void onDisable() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            resetDeathSpectating(player);
        }
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {
        final Player player = event.getEntity();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        deathLock.startRespawnTimer(player, this::onRespawn);

        if (plugin.getConfig().getBoolean(GimmeHardcore.CONFIG_RESPAWN_AUTO)) {
            new BukkitRunnable() {
                public void run() {
                    player.spigot().respawn();
                }
            }.runTaskLater(plugin, plugin.getConfig().getLong(GimmeHardcore.CONFIG_RESPAWN_AUTO_DELAY));
        }
    }

    @EventHandler
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        final Player player = event.getPlayer();

        if (!deathLock.isDead(player)) return;

        Location respawnLocation = event.getRespawnLocation();
        Location deathLocation = deathLock.getDeathData(player).deathLocation;
        if (deathLocation.getY() < -40) deathLocation.setY(-40);

        event.setRespawnLocation(deathLocation);

        deathSpectating.deathSpectate(player, deathLocation, respawnLocation, deathLock.getDeathData(player));
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        resetDeathSpectating(event.getPlayer());
    }

    private void resetDeathSpectating(@NotNull Player player) {
        if (awaitAction.isAwaitingAction(player)) awaitAction.stopAwaitAction(player);
        if (deathSpectating.isDeathSpectating(player)) deathSpectating.unDeathSpectate(player);
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.isDead()) return;
        if (!deathLock.isDead(player)) return;

        deathSpectating.deathSpectate(player, player.getLocation(), player.getLocation(), deathLock.getDeathData(player));
    }


    @EventHandler
    private void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if (!deathSpectating.isDeathSpectating(player) || maxSpectatorDistance < 0) return;

        event.setCancelled(true);
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (deathSpectating.isDeathSpectating(player)) {
            if (maxSpectatorDistance < 0) return;
            if (event.getTo() == null) {
                event.setCancelled(true);
                return;
            }
            Location spectateLocation = deathSpectating.getSpectateLocation(player);
            if (maxSpectatorDistance == 0 || event.getTo().distanceSquared(spectateLocation) > Math.pow(maxSpectatorDistance, 2)) {
                //Location to = event.getFrom();
                //to.setPitch(event.getTo().getPitch());
                //to.setYaw(event.getTo().getYaw());
                //event.setTo(to);
                event.setCancelled(true);
            }
        }

        if (awaitAction.isAwaitingAction(player)) {
            if (event.getTo() != null && event.getTo().distance(event.getFrom()) > 0.01d) {
                awaitAction.stopAwaitAction(player);
            }
        }
    }


    private void onRespawn(Player player) {
        if (!deathSpectating.isDeathSpectating(player)) {
            awaitAction.clearRespawnMessages(player);
            return;
        }

        deathSpectating.unDeathSpectate(player);
        awaitAction.awaitAction(player);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("hc")) {
            if (args.length >= 1 && args[0].equalsIgnoreCase("respawn")) {
                if (args.length == 1) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        if (deathLock.respawn(player)) {
                            sender.sendMessage(ChatColor.GREEN + "Respawned yourself");
                            return true;
                        }
                    }
                } else if (args.length == 2) {
                    Player p = Bukkit.getPlayer(args[1]);
                    if (p != null && deathLock.respawn(p)) {
                        sender.sendMessage(ChatColor.GREEN + "Respawned " + p.getName());
                        return true;
                    }
                }

            }
        }
        return false;
    }

}
