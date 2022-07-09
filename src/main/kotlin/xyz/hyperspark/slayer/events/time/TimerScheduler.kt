package xyz.hyperspark.slayer.events.time

import arrow.core.*
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import java.util.*

/**
 * A scheduler for [TimerTask] tasks.
 */
class TimerScheduler(
	private val instance: Plugin,
	private val worldName: String,
) {
	private var clockTimers: EnumMap<TimerInterval, BukkitTask> = EnumMap(TimerInterval::class.java)

	/**
	 * Enable the scheduler and any defined timers.
	 */
	fun registerTimers() {
		registerTimer(TimerInterval.TenSeconds)
	}

	/**
	 * Disable the scheduler and any defined timers.
	 */
	fun deregisterTimers() {
		deregisterTimer(TimerInterval.TenSeconds)
	}

	/**
	 * Enable the specified timer, if not already enabled.
	 * @param interval [TimerInterval] to disable.
	 */
	private fun registerTimer(interval: TimerInterval) {
		when(this.clockTimers.getOrNone(interval)) {
			is None -> {
				val newTimer = TimerTask(this.instance.server.pluginManager, interval)

				val ticksUntilFirstRun = Option
					.fromNullable(this.instance.server.getWorld(worldName))
					.map { it.time % interval.ticks.value }
					.getOrElse { 0 }

				val scheduler = this.instance.server.scheduler
				val task = scheduler.runTaskTimer(this.instance, newTimer, ticksUntilFirstRun, interval.ticks.value)

				this.clockTimers[interval] = task
				this.instance.logger.info("Enabled ${interval.name} timer.")
			}
			is Some -> {
				this.instance.logger.warning("${interval.name} timer already enabled!")
			}
		}
	}

	/**
	 * Disable the specified timer, if enabled.
	 * @param interval [TimerInterval] to disable.
	 */
	private fun deregisterTimer(interval: TimerInterval) {
		when(val timers = this.clockTimers.getOrNone(interval)) {
			is Some -> {
				timers.value.cancel()
				this.clockTimers.remove(interval)
				this.instance.logger.info("Disabled ${interval.name} timer.")
			}
			is None -> this.instance.logger.info("Timer ${interval.name} already disabled.")
		}
	}
}
