package me.gimme.gimmehardcore.listeners;

import me.gimme.gimmehardcore.GimmeHardcore;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageTakenModifier implements Listener {

    private FileConfiguration config;

    public DamageTakenModifier(FileConfiguration config) {
        this.config = config;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        String damageSourceNamespaceId;

        if (damager.getType().equals(EntityType.PLAYER)) {
            damageSourceNamespaceId = ((HumanEntity) damager).getInventory().getItemInMainHand().getType().getKey().getKey();
            event.setDamage(event.getDamage() * getDamageModifier(event.getEntityType(), damageSourceNamespaceId));
        }

        damageSourceNamespaceId = damager.getType().getKey().getKey();
        event.setDamage(event.getDamage() * getDamageModifier(event.getEntityType(), damageSourceNamespaceId));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onEntityDamageEvent(EntityDamageEvent event) {
        event.setDamage(event.getDamage() * getDamageModifier(event.getEntityType(), event.getCause().name()));
    }

    private double getDamageModifier(EntityType entityType, String damageSource) {
        String entityNamespaceId = entityType.getKey().getKey();

        ConfigurationSection entities = config.getConfigurationSection(GimmeHardcore.CONFIG_DAMAGE_TAKEN_MODIFIER);
        String defaultEntityPath = "default";

        if (entities == null) return 1;
        Double foundModifierValue = findModifierValue(entities, entityNamespaceId, damageSource);
        if (foundModifierValue == null) foundModifierValue = findModifierValue(entities, defaultEntityPath, damageSource);
        if (foundModifierValue == null) return 1;

        return foundModifierValue;
    }

    private Double findModifierValue(ConfigurationSection entitiesSection, String entityNamespaceId, String damageSourceNamespaceId) {
        if (!entitiesSection.contains(entityNamespaceId)) return null;

        String defaultValuePath = "default";
        ConfigurationSection valuesSection = entitiesSection.getConfigurationSection(entityNamespaceId);

        if (valuesSection.contains(damageSourceNamespaceId)) {
            return valuesSection.getDouble(damageSourceNamespaceId);
        } else if (valuesSection.contains(defaultValuePath)) {
            return valuesSection.getDouble(defaultValuePath);
        } else {
            return null;
        }
    }

}
