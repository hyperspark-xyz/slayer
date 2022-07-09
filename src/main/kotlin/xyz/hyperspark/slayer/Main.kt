package xyz.hyperspark.slayer

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin
import xyz.hyperspark.slayer.events.EventManager
import xyz.hyperspark.slayer.events.listeners.SlayerKillListener
import xyz.hyperspark.slayer.events.listeners.SlayerListener
import xyz.hyperspark.slayer.events.listeners.SlayerMobSpawnListener
import xyz.hyperspark.slayer.events.time.TimerScheduler
import xyz.hyperspark.slayer.models.SlayerState


class Main: JavaPlugin() {
    private val timer: TimerScheduler = TimerScheduler(this, "world");

    override fun onEnable() {
        // Plugin startup logic
        super.onEnable();

        val pluginManager = Bukkit.getPluginManager()

        val world = this.server.getWorld("world") ?: return

        logger.info("World found!")

        val rsp = server.servicesManager.getRegistration(Economy::class.java) ?: return
        val econ = rsp.provider

        logger.info("Econ found!")

        val eventManager = EventManager(this, pluginManager)
        val slayerState = SlayerState.empty()
        val pluginTag = "${ChatColor.DARK_AQUA}[${ChatColor.RESET}Slayer${ChatColor.DARK_AQUA}]${ChatColor.RESET}"

        eventManager.registerListener(
            SlayerListener(
                pluginTag,
                world,
                { this.server.onlinePlayers.toSet() },
                slayerState,
                econ
            )
        )

        eventManager.registerListener(
            SlayerKillListener(
                pluginTag,
                slayerState,
            )
        )

        eventManager.registerListener(
            SlayerMobSpawnListener(
                this,
            )
        )

        this.timer.registerTimers()
    }

    override fun onDisable() {
        // Plugin shutdown logic
        super.onDisable();

        this.timer.deregisterTimers()
    }
}
