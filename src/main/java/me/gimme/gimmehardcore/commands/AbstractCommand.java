package me.gimme.gimmehardcore.commands;

import me.gimme.gimmehardcore.GimmeHardcore;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.Plugin;

public abstract class AbstractCommand implements CommandExecutor {

    protected Plugin plugin;

    public AbstractCommand() {
        plugin = GimmeHardcore.getPlugin(GimmeHardcore.class);
    }

}
