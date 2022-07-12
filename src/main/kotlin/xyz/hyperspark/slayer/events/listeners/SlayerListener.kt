package xyz.hyperspark.slayer.events.listeners

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import net.milkbowl.vault.economy.Economy
import org.bukkit.ChatColor
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import xyz.hyperspark.slayer.events.TimerEvent
import xyz.hyperspark.slayer.events.time.TimerInterval
import xyz.hyperspark.slayer.models.*

class SlayerListener(
    private val pluginTag: String,
    private val world: World,
    private val getPlayers: () -> Set<Player>,
    private val slayerState: SlayerState,
    private val economy: Economy,
): Listener {
    @EventHandler
    fun onTimerEvent(evt: TimerEvent) {
        if (evt.interval != TimerInterval.TenSeconds) return

        if (this.world.isDayTime()) {
            if (this.slayerState.status == SlayerStatus.InProgress) {
                val players = this.getPlayers()
                val winners = calculateWinners(slayerState.scores);
                announceFinish(pluginTag, players, winners);
                awardPlayers(pluginTag, this.economy, players, winners);

                slayerState.reset()
            }
        } else if (this.slayerState.status == SlayerStatus.NotInProgress) {
            val players = this.getPlayers()
            announceStart(pluginTag, players)

            this.slayerState.status = SlayerStatus.InProgress
        }
    }
}

private val FirstPlacePrize: Double = 500.0
private val SecondPlacePrize: Double = 200.0
private val ThirdPlacePrize: Double = 100.0
private val ParticipationPrize: Double = 20.0

data class TopThree(
    val first: Option<Pair<PlayerId, ScoreCard>>,
    val second: Option<Pair<PlayerId, ScoreCard>>,
    val third: Option<Pair<PlayerId, ScoreCard>>,
)

data class Winners (
    val topThree: TopThree,
    val killedSomething: Map<PlayerId, ScoreCard>
)

private fun calculateWinners(scores: Map<PlayerId, ScoreCard>): Winners {
    val topThreeEntries = scores.toList()
        .sortedByDescending { (_, scoreCard) -> scoreCard.pointsTotal.value }
        .map { Some(it) }
        .take(3)

    val topThreeIds = topThreeEntries.map { it.value.first }

    val killedSomething = scores
        .filter { (_, scoreCard) -> scoreCard.kills.isNotEmpty() }
        .filter { (playerId, _) -> !topThreeIds.contains(playerId) }
        .toMap();

    return Winners(
        topThree = TopThree(
            first = topThreeEntries.getOrElse(0) { None },
            second = topThreeEntries.getOrElse(1) { None },
            third = topThreeEntries.getOrElse(2) { None }
        ),
        killedSomething,
    )
}

private fun formatEntityName(type: EntityType): String {
    return type.name
        .lowercase()
        .replace('_', ' ')
        .split(' ')
        .joinToString(" ") { mobNamePart ->
            mobNamePart.replaceFirstChar { it.uppercase() }
        }
}

private fun announceStart(pluginTag: String, players: Set<Player>) {
    players.forEach {
        it.sendMessage("$pluginTag Slayer has started for this evening! Get those mobs!")
    }
}

private fun getKillSummaryMessage(kills: List<Kill>): String {
    val killCountByEntityType = kills.groupingBy { it.entityType }.eachCount()
    val messageLines = killCountByEntityType.toList()
        .sortedByDescending { (_, killCount) -> killCount }
        .take(3)
        .map { (entity, killCount) -> "-> $killCount ${formatEntityName(entity)}" }

    return messageLines.joinToString("\n")
}

private fun announceFinish(pluginTag: String, players: Set<Player>, winners: Winners) {
    val sb = StringBuilder()
    val playersById = players.associateBy { it.uniqueId }
    val ( topThree ) = winners;

    sb.appendLine("$pluginTag Slayer has finished for this evening!")

    if (topThree.first.isDefined()) {
        val (first) = topThree.first as Some
        val (id, scoreCard) = first
        val name = playersById[id.value]?.name ?: "Unknown"
        val killSummary = getKillSummaryMessage(scoreCard.kills)

        sb.appendLine("1. ${ChatColor.BOLD}$name${ChatColor.RESET} (${scoreCard.pointsTotal.value})")
        sb.appendLine(killSummary) // Only for champ
    }

    if (topThree.second.isDefined()) {
        val (second) = topThree.second as Some
        val (id, scoreCard) = second
        val name = playersById[id.value]?.name ?: "Unknown"

        sb.appendLine("2. ${ChatColor.BOLD}$name${ChatColor.RESET} (${scoreCard.pointsTotal.value})")
    }

    if (topThree.third.isDefined()) {
        val (third) = topThree.third as Some
        val (id, scoreCard) = third
        val name = playersById[id.value]?.name ?: "Unknown"

        sb.appendLine("3. ${ChatColor.BOLD}$name${ChatColor.RESET} (${scoreCard.pointsTotal.value})")
    }

    val nonWinners = (winners.killedSomething
        .toList()
        // Convert each Pair<A, B> to an Option<Pair<A, B>>
        .map<Pair<PlayerId, ScoreCard>, Option<Pair<PlayerId, ScoreCard>>> { Some(it) } +
            topThree.second + // Add second place
            topThree.third) // Add third place.
        // Remove Nones and produce a set
        .fold(emptyMap<PlayerId, ScoreCard>()) { acc, item ->
            item.fold({ acc }, { acc + it })
        }

    val message = sb.toString()

    players.forEach {
        val playerSb = StringBuilder()

        val maybeParticipantCard = Option.fromNullable(
            nonWinners[PlayerId(it.uniqueId)]
        )

        playerSb.appendLine(message)
        playerSb.appendLine()

        if (maybeParticipantCard.isDefined()) {
            val ( participantCard ) = maybeParticipantCard as Some;
            val summary = getKillSummaryMessage(participantCard.kills)

            playerSb.appendLine("Your Slayer summary:")
            playerSb.appendLine(summary)
        }

        it.sendMessage(playerSb.toString())
    }
}

private fun awardPlayers(pluginTag: String, economy: Economy, players: Set<Player>, winners: Winners) {
    val playersById = players.associateBy { it.uniqueId }
    val (topThree, participants) = winners;

    if (topThree.first.isDefined()) {
        val (first) = topThree.first as Some
        val (id, _) = first
        val maybePlayer = Option.fromNullable(playersById[id.value])

        if (maybePlayer.isDefined()) {
            val (player) = maybePlayer as Some
            economy.depositPlayer(player, FirstPlacePrize)
            player.sendMessage("$pluginTag You won! You've been awarded $$FirstPlacePrize!")
        }
    }

    if (topThree.second.isDefined()) {
        val (second) = topThree.second as Some
        val (id, _) = second
        val maybePlayer = Option.fromNullable(playersById[id.value])

        if (maybePlayer.isDefined()) {
            val (player) = maybePlayer as Some
            economy.depositPlayer(player, SecondPlacePrize)
            player.sendMessage("$pluginTag So close! You got second place! You've been awarded $$SecondPlacePrize!")
        }
    }

    if (topThree.third.isDefined()) {
        val (third) = topThree.third as Some
        val (id, _) = third
        val maybePlayer = Option.fromNullable(playersById[id.value])

        if (maybePlayer.isDefined()) {
            val (player) = maybePlayer as Some
            economy.depositPlayer(player, ThirdPlacePrize)
            player.sendMessage("$pluginTag You got third place! You've been awarded $$ThirdPlacePrize!")
        }
    }

    participants.forEach {
        val maybePlayer = Option.fromNullable(playersById[it.key.value])

        if (maybePlayer.isDefined()) {
            val (player) = maybePlayer as Some
            economy.depositPlayer(player, ParticipationPrize)
            player.sendMessage("$pluginTag Better luck next time! You've been awarded $$ParticipationPrize for participating!")
        }
    }
}

fun World.isDayTime(): Boolean {
    return this.time in 1..12574
}

fun World.isNightTime(): Boolean {
    return !this.isDayTime()
}