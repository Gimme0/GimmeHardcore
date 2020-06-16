package me.gimme.gimmehardcore.listeners;

import me.gimme.gimmehardcore.GimmeHardcore;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UnstackableItems implements Listener {

    private Set<String> unstackableItems = new HashSet<>();

    public UnstackableItems(@NotNull FileConfiguration config) {
        unstackableItems.addAll(config.getStringList(GimmeHardcore.CONFIG_UNSTACKABLE_ITEMS));
    }

    /**
     * Makes certain items unstackable.
     */
    @EventHandler(priority = EventPriority.LOW)
    private void onItemSpawn(ItemSpawnEvent event) {
        if (event.isCancelled()) return;

        Item entity = event.getEntity();
        ItemStack itemStack = entity.getItemStack();
        Material type = itemStack.getType();
        if (!unstackableItems.contains(type.getKey().getKey())) return;

        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return;
        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();
        if (!lore.isEmpty()) return;

        lore.add(encodeInvisibleString(entity.getUniqueId().toString()));

        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }

    /**
     * @return an invisible version of the specified string
     */
    private String encodeInvisibleString(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            sb.append('§').append(c);
        }
        return sb.toString();
    }

}
