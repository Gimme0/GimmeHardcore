package me.gimme.gimmehardcore.listeners;

import me.gimme.gimmehardcore.GimmeHardcore;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class WrathOfTheNether implements Listener {
    private FileConfiguration config;

    public WrathOfTheNether(@NotNull FileConfiguration config) {
        this.config = config;
    }

    /**
     * Activates WotN when a player has been in the Nether for a certain amount of time.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onEnterNether(PlayerTeleportEvent event) {
        if (event.isCancelled()) return;
        if (!event.getCause().equals(PlayerTeleportEvent.TeleportCause.NETHER_PORTAL)) return;
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)
        || event.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) return;

        Location to = event.getTo();
        if (to == null) return;

        World toWorld = to.getWorld();
        if (toWorld == null) return;

        if (!toWorld.getEnvironment().equals(World.Environment.NETHER)) return;

        sendOnEnterNetherMessage(event.getPlayer());
    }

    /**
     * Activates WotN when a player attacks an entity in the Nether.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;

        World world = event.getEntity().getWorld();
        Entity damager = event.getDamager();
        EntityType damagedType = event.getEntityType();
        if (!world.getEnvironment().equals(World.Environment.NETHER)) return;
        if (damagedType.equals(EntityType.PLAYER) || damagedType.equals(EntityType.FIREBALL)) return;

        if (damager.getType().equals(EntityType.PLAYER)) {
            if (((Player) damager).getGameMode().equals(GameMode.CREATIVE)) return;
        } else if (damager instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            if (!(arrow.getShooter() instanceof Player)) return;
            if (((Player) arrow.getShooter()).getGameMode().equals(GameMode.CREATIVE)) return;
        } else if (!(damager.getType().equals(EntityType.FIREBALL) && damagedType.equals(EntityType.GHAST))) {
            return;
        }

        activateWrathOfTheNether(world);
    }

    /**
     * Activates WotN when a player steals a Nether Quartz Ore.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onNetherQuartzOreBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        if (!event.getBlock().getType().equals(Material.NETHER_QUARTZ_ORE)) return;

        activateWrathOfTheNether(event.getBlock().getWorld());
    }

    private void activateWrathOfTheNether(@NotNull World world) {
        if (angerAllPigmen(world)) {
            world.getPlayers().forEach(this::sendOnWrathMessage);
        }
    }

    private boolean angerAllPigmen(@NotNull World world) {
        int angry = 0;
        int nonAngry = 0;

        for (PigZombie pigZombie : world.getEntitiesByClass(PigZombie.class)) {
            if (pigZombie.isAngry()) angry++;
            else nonAngry++;
            pigZombie.setAngry(true);
        }

        return nonAngry > angry;
    }

    private void sendOnEnterNetherMessage(@NotNull Player player) {
        player.sendTitle(
                config.getString(GimmeHardcore.CONFIG_WRATH_ENTER_NETHER_MESSAGE_TITLE),
                config.getString(GimmeHardcore.CONFIG_WRATH_ENTER_NETHER_MESSAGE_SUBTITLE),
                0, 80, 40);
    }

    private void sendOnWrathMessage(@NotNull Player player) {
        player.sendTitle(
                config.getString(GimmeHardcore.CONFIG_WRATH_MESSAGE_TITLE),
                config.getString(GimmeHardcore.CONFIG_WRATH_MESSAGE_SUBTITLE),
                0, 60, 20);
    }
}
