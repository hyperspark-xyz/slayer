package xyz.hyperspark.slayer.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import xyz.hyperspark.slayer.events.time.TimerInterval
import xyz.hyperspark.slayer.events.time.TimerTask

/**
 * An event fired by a [TimerTask].
 */
class TimerEvent(
	private val timerInterval: TimerInterval
): Event() {
	override fun getHandlers(): HandlerList {
		return HandlerList
	}

	/**
	 * Get the [TimerInterval] for this [TimerEvent].
	 */
	val interval: TimerInterval
		get() = this.timerInterval

	/**
	 * A companion object to satisfy the static getHandlersList.
	 * This is referenced in [this post](https://www.spigotmc.org/threads/kotlin-cant-we-create-custom-event.367640/) and
	 * [this post](https://stackoverflow.com/questions/40352684/what-is-the-equivalent-of-java-static-methods-in-kotlin).
	 */
	companion object {
		private val HandlerList = HandlerList()

		@JvmStatic
		fun getHandlerList(): HandlerList {
			return HandlerList
		}
	}
}
