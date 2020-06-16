package me.gimme.gimmehardcore.unused.advancements;

import me.gimme.gimmehardcore.GimmeHardcore;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.Locale;
import java.util.logging.Level;

public abstract class AbstractAdvancement implements Listener {

    private enum  Tab {
        HARDCORE
    }

    protected enum Hardcore {
        KILL_CREEPER_WITH_FISTS,
        KILL_PIGMEN,
        DIE
    }

    private String advancementName;
    private String advancementPath;
    protected Plugin plugin;

    public AbstractAdvancement(Hardcore advancement) {
        this(Tab.HARDCORE + "/" + advancement);
    }

    private AbstractAdvancement(String advancementName) {
        this.advancementName = advancementName.toLowerCase(Locale.ROOT);
        plugin = GimmeHardcore.getPlugin(GimmeHardcore.class);
        advancementPath = (plugin.getName() + ":" + advancementName).toLowerCase(Locale.ROOT);
    }

    protected void grantAdvancement(Player player) {
        if (hasAdvancement(player)) return;
        String command = "advancement grant " + player.getName() + " only " + advancementPath;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    protected boolean hasAdvancement(Player player) {
        NamespacedKey nsk = new NamespacedKey(plugin, advancementName);
        Advancement advancement = plugin.getServer().getAdvancement(nsk);
        if (advancement == null) {
            plugin.getLogger().log(Level.WARNING, "Couldn't find advancement: " + nsk);
            return false;
        }
        return player.getAdvancementProgress(advancement).isDone();
    }

}
