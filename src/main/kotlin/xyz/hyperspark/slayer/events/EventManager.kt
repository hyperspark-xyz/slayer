package xyz.hyperspark.slayer.events

import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import xyz.hyperspark.slayer.events.abstractions.EventDispatcher

class EventManager(
    private val plugin: Plugin,
    private val pluginManager: PluginManager
): EventDispatcher {
    override fun registerListener(listener: Listener): EventDispatcher {
        this.pluginManager.registerEvents(listener, plugin)
        return this;
    }

    override fun deregisterListener(listener: Listener): EventDispatcher {
        HandlerList.unregisterAll(listener)
        return this;
    }
}