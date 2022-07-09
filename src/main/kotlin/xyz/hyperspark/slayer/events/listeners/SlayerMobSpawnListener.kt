package xyz.hyperspark.slayer.events.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.plugin.Plugin

class SlayerMobSpawnListener(
    private val plugin: Plugin
): Listener {
    @EventHandler
    fun onMobSpawn(evt: CreatureSpawnEvent) {
        evt.entity.setMetadata("SPAWN_REASON", FixedMetadataValue(plugin, evt.spawnReason))
    }
}