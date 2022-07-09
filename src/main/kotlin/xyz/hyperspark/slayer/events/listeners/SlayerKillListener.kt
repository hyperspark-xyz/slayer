package xyz.hyperspark.slayer.events.listeners

import org.bukkit.ChatColor
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDeathEvent
import xyz.hyperspark.slayer.models.PlayerId
import xyz.hyperspark.slayer.models.Points
import xyz.hyperspark.slayer.models.SlayerState
import xyz.hyperspark.slayer.models.SlayerStatus

class SlayerKillListener(
    private val pluginTag: String,
    private val slayerState: SlayerState
): Listener {
    private val killScores: Map<EntityType, Points> =
        mapOf(
            Pair(EntityType.ENDERMAN, Points(50)),
            Pair(EntityType.VINDICATOR, Points(40)),
            Pair(EntityType.RAVAGER, Points(35)),
            Pair(EntityType.EVOKER, Points(35)),
            Pair(EntityType.BLAZE, Points(30)),
            Pair(EntityType.GUARDIAN, Points(30)),
            Pair(EntityType.WITHER_SKELETON, Points(25)),
            Pair(EntityType.CREEPER, Points(20)),
            Pair(EntityType.WITCH, Points(20)),
            Pair(EntityType.VEX, Points(20)),
            Pair(EntityType.CAVE_SPIDER, Points(15)),

            // Boss mobs don't count
            Pair(EntityType.WITHER, Points(0)),
            Pair(EntityType.ENDER_DRAGON, Points(0)),
            Pair(EntityType.WARDEN, Points(0)),
        )

    @EventHandler
    fun onKill(evt: EntityDeathEvent) {
        if (!evt.entity.hasMetadata("SPAWN_REASON")) return
        if (evt.entity.getMetadata("SPAWN_REASON").any { (it.value() as CreatureSpawnEvent.SpawnReason) === CreatureSpawnEvent.SpawnReason.SPAWNER }) return
        if (slayerState.status == SlayerStatus.NotInProgress) return

        if (evt.entity is Monster) {
            val killer = evt.entity.killer ?: return
            val playerId = PlayerId(killer.uniqueId)

            val currentPoints = slayerState.scores[playerId] ?: Points(0)
            val pointsToAward = killScores[evt.entityType] ?: Points(10)
            val newPoints = currentPoints.copy(value = currentPoints.value + pointsToAward.value)

            killer.sendMessage("$pluginTag You killed a ${evt.entityType.name.lowercase().replaceFirstChar { it.uppercase() }}! +${pointsToAward.value} points!")

            slayerState.scores[playerId] = newPoints
        }
    }
}