package xyz.hyperspark.slayer.events.time

import xyz.hyperspark.slayer.models.time.Ticks

/**
 * Represents an interval of time within the game world.
 * The values can be divided by 20 to give the interval in seconds.
 */
enum class TimerInterval(val ticks: Ticks) {
	/**
	 * Every ten seconds
	 */
	TenSeconds(Ticks(200)),
}
