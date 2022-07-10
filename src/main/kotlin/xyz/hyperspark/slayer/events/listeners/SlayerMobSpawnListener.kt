package xyz.hyperspark.slayer.events.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.plugin.Plugin

class SlayerMobSpawnListener(
    private val plugin: Plugin
): Listener {
    companion object SlayerMobSpawnListener {
        const val MetadataKey = "SPAWN_REASON"
    }

    @EventHandler
    fun onMobSpawn(evt: CreatureSpawnEvent) {
        evt.entity.setMetadata(MetadataKey, FixedMetadataValue(plugin, evt.spawnReason))
    }
}