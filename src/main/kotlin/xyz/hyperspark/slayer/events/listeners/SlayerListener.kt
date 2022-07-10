package xyz.hyperspark.slayer.events.listeners

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import net.milkbowl.vault.economy.Economy
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import xyz.hyperspark.slayer.events.TimerEvent
import xyz.hyperspark.slayer.models.PlayerId
import xyz.hyperspark.slayer.models.Points
import xyz.hyperspark.slayer.models.SlayerState
import xyz.hyperspark.slayer.models.SlayerStatus

class SlayerListener(
    private val pluginTag: String,
    private val world: World,
    private val getPlayers: () -> Set<Player>,
    private val slayerState: SlayerState,
    private val economy: Economy,
): Listener {
    @EventHandler
    fun onTimerEvent(evt: TimerEvent) {
        if (this.world.isDayTime()) {
            if (this.slayerState.status == SlayerStatus.InProgress) {
                val players = this.getPlayers()
                val winners = calculateWinners(slayerState.scores);
                announceFinish(pluginTag, players, winners.topThree);
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
    val first: Option<Pair<PlayerId, Points>>,
    val second: Option<Pair<PlayerId, Points>>,
    val third: Option<Pair<PlayerId, Points>>,
)

data class Winners (
    val topThree: TopThree,
    val killedSomething: Set<PlayerId>
)

private fun calculateWinners(scores: Map<PlayerId, Points>): Winners {
    val topThreeEntries = scores.toList()
        .sortedByDescending { (_, value) -> value }
        .map { Some(it) }
        .take(3)

    val topThreeIds = topThreeEntries.map { it.value.first }

    val killedSomething = scores
        .filter { (_, score) -> score.value > 0 }
        .filter { (playerId, _) -> !topThreeIds.contains(playerId) }
        .keys;

    return Winners(
        topThree = TopThree(
            first = topThreeEntries.getOrElse(0) { None },
            second = topThreeEntries.getOrElse(1) { None },
            third = topThreeEntries.getOrElse(2) { None }
        ),
        killedSomething,
    )
}

private fun announceStart(pluginTag: String, players: Set<Player>) {
    players.forEach {
        it.sendMessage("$pluginTag Slayer has started for this evening! Get those mobs!")
    }
}

private fun announceFinish(pluginTag: String, players: Set<Player>, topThree: TopThree) {
    val sb = StringBuilder()
    val playersById = players.associateBy { it.uniqueId }

    sb.appendLine("$pluginTag Slayer has finished for this evening!")

    if (topThree.first.isDefined()) {
        val (first) = topThree.first as Some
        val (id, points) = first
        val name = playersById[id.value]?.name ?: "Unknown"
        sb.appendLine("1. $name (${points.value})")
    }

    if (topThree.second.isDefined()) {
        val (second) = topThree.second as Some
        val (id, points) = second
        val name = playersById[id.value]?.name ?: "Unknown"
        sb.appendLine("2. $name (${points.value})")
    }

    if (topThree.third.isDefined()) {
        val (third) = topThree.third as Some
        val (id, points) = third
        val name = playersById[id.value]?.name ?: "Unknown"
        sb.appendLine("3. $name (${points.value})")
    }

    val message = sb.toString()

    players.forEach {
        it.sendMessage(message)
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
        val maybePlayer = Option.fromNullable(playersById[it.value])

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