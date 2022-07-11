package xyz.hyperspark.slayer.models

import java.util.UUID

data class Points(val value: Long): Comparable<Points> {
    override fun compareTo(other: Points): Int = this.value.compareTo(other.value)

}
data class PlayerId(val value: UUID)

data class ScoreCard(
    val pointsTotal: Points,
    val kills: List<Kill>,
)

enum class SlayerStatus {
    InProgress,
    NotInProgress,
}

data class SlayerState(val scores: MutableMap<PlayerId, ScoreCard>, var status: SlayerStatus) {
    fun reset() {
        this.scores.clear()
        this.status = SlayerStatus.NotInProgress
    }

    companion object SlayerState {
        fun empty(): xyz.hyperspark.slayer.models.SlayerState {
            return SlayerState(
                scores = mutableMapOf(),
                status = SlayerStatus.NotInProgress
            )
        }
    }
}
