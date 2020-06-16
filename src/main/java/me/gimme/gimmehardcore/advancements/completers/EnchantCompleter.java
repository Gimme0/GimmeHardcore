package me.gimme.gimmehardcore.advancements.completers;

import me.gimme.gimmehardcore.advancements.Completer;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EnchantCompleter extends Completer {

    private Set<Material> types = new HashSet<>();

    public EnchantCompleter(@NotNull Material... types) {
        Collections.addAll(this.types, types);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onEnchant(EnchantItemEvent event) {
        if (event.isCancelled()) return;
        if (!types.contains(event.getItem().getType())) return;

        grant(event.getEnchanter());
    }

}
