package me.gimme.gimmehardcore.listeners;

import me.gimme.gimmehardcore.GimmeHardcore;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public abstract class AbstractEventListener implements Listener {

    protected Plugin plugin;

    public AbstractEventListener() {
        plugin = GimmeHardcore.getPlugin(GimmeHardcore.class);
    }

}
