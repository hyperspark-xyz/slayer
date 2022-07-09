package xyz.hyperspark.slayer.events.time

import org.bukkit.plugin.PluginManager
import xyz.hyperspark.slayer.events.TimerEvent

/**
 * A task to push [TimerEvent] events on a specified [TimerInterval].
 */
class TimerTask(
	private val pluginManager: PluginManager,
	private val interval: TimerInterval,
): Runnable {
	override fun run() {
		val event = TimerEvent(interval)
		pluginManager.callEvent(event)
	}
}
