package xyz.hyperspark.slayer.events.listeners

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDeathEvent
import xyz.hyperspark.slayer.models.*

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

    private fun getPointsForKill(kill: Kill): Points {
        return killScores[kill.entityType] ?: Points(10)
    }

    private fun addKillToScoreCard(scoreCard: ScoreCard, kill: Kill): ScoreCard {
        val currentPoints = scoreCard.pointsTotal
        val currentKills = scoreCard.kills
        val pointsToAward = getPointsForKill(kill)

        val newPoints = currentPoints.copy(value = currentPoints.value + pointsToAward.value)
        val newKills = currentKills + kill

        return scoreCard.copy(
            pointsTotal = newPoints,
            kills = newKills
        )
    }

    @EventHandler
    fun onKill(evt: EntityDeathEvent) {
        if (slayerState.status == SlayerStatus.NotInProgress) return

        if (evt.entity is Monster) {
            if (!evt.entity.hasMetadata(SlayerMobSpawnListener.MetadataKey)) return
            if (evt.entity.getMetadata(SlayerMobSpawnListener.MetadataKey).any { (it.value() as CreatureSpawnEvent.SpawnReason) === CreatureSpawnEvent.SpawnReason.SPAWNER }) return

            val killer = evt.entity.killer ?: return
            val entityType = evt.entityType
            val playerId = PlayerId(killer.uniqueId)

            val scoreCard = slayerState.scores[playerId] ?: ScoreCard(Points(0), listOf())
            val kill = Kill(playerId, entityType)

            slayerState.scores[playerId] = addKillToScoreCard(scoreCard, kill)

            val pointsForKill = getPointsForKill(kill)
            val chatComponent = TextComponent.fromLegacyText(
                "$pluginTag You killed a ${evt.entityType.name.lowercase().replaceFirstChar { it.uppercase() }}! +${pointsForKill.value} points!"
            )

            killer.spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                *chatComponent
            )
        }
    }
}