package com.creeperface.nukkit.placeholderapi

import cn.nukkit.AdventureSettings
import cn.nukkit.Nukkit
import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.entity.Entity
import cn.nukkit.event.Listener
import com.creeperface.nukkit.placeholderapi.api.Placeholder
import com.creeperface.nukkit.placeholderapi.api.util.MatchedGroup
import com.creeperface.nukkit.placeholderapi.api.util.matchPlaceholders
import com.creeperface.nukkit.placeholderapi.placeholder.StaticPlaceHolder
import com.creeperface.nukkit.placeholderapi.placeholder.VisitorSensitivePlaceholder
import com.creeperface.nukkit.placeholderapi.util.bytes2MB
import com.creeperface.nukkit.placeholderapi.util.formatAsTime
import com.creeperface.nukkit.placeholderapi.util.round
import com.google.common.base.Preconditions
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import com.creeperface.nukkit.placeholderapi.api.PlaceholderAPI as API

/**
 * @author CreeperFace
 */
class PlaceholderAPIIml private constructor(private val plugin: PlaceholderPlugin) : Listener, API {

    private val placeholders = mutableMapOf<String, Placeholder<out Any?>>()
    private val updatePlaceholders = mutableMapOf<String, Placeholder<out Any?>>()

    private val configuration: Configuration

    companion object {

        @JvmStatic
        lateinit var instance: API
            private set

        private var initialized = false //stupid kotlin bug

        @JvmStatic
        fun createInstance(plugin: PlaceholderPlugin): API {
            Preconditions.checkState(!initialized, "PlaceholderAPI was already initialized")

            instance = PlaceholderAPIIml(plugin)
            initialized = true
            return instance
        }
    }

    init {
        registerDefaultPlaceholders()
        configuration = Configuration(plugin)
        configuration.load()
    }

    internal fun init() {
        plugin.server.pluginManager.registerEvents(this, plugin)

        plugin.server.scheduler.scheduleRepeatingTask(plugin, { updatePlaceholders() }, configuration.updateInterval)
    }

    override fun <T> staticPlaceholder(name: String, loader: () -> T?, vararg aliases: String) {
        staticPlaceholder(name, loader, 20, false, *aliases)
    }

    override fun <T> staticPlaceholder(name: String, loader: () -> T?, updateInterval: Int, autoUpdate: Boolean, vararg aliases: String) {
        registerPlaceholder(StaticPlaceHolder(name, updateInterval, autoUpdate, aliases.toSet(), loader))
    }

    override fun <T> visitorSensitivePlaceholder(name: String, loader: (Player) -> T?, vararg aliases: String) {
        visitorSensitivePlaceholder(name, loader, 20, false, *aliases)
    }

    override fun <T> visitorSensitivePlaceholder(name: String, loader: (Player) -> T?, updateInterval: Int, autoUpdate: Boolean, vararg aliases: String) {
        registerPlaceholder(VisitorSensitivePlaceholder(name, updateInterval, autoUpdate, aliases.toSet(), loader))
    }

    override fun registerPlaceholder(placeholder: Placeholder<out Any?>) {
        val existing = this.placeholders.putIfAbsent(placeholder.name, placeholder)
        Preconditions.checkState(existing != placeholder, "Trying to register placeholder '${placeholder.name}' which already exists")

        placeholder.aliases.forEach {
            val v = this.placeholders.putIfAbsent(it, placeholder)

            if (v != null && v != placeholder) {
                plugin.logger.warning("Placeholder '${placeholder.name}' tried to register alias '$it' which is already used by a hologram '${v.name}'")
            }
        }

        if (placeholder.updateInterval > 0 && placeholder.autoUpdate) {
            updatePlaceholders[placeholder.name] = placeholder
        }
    }

    override fun getValue(key: String, visitor: Player?, defaultValue: String?): String? =
            placeholders[key]?.getValue(visitor) ?: key

    override fun translateString(input: String, visitor: Player?) =
            translateString(input, visitor, input.matchPlaceholders())

    override fun translateString(input: String, visitor: Player?, matched: Collection<MatchedGroup>): String {
        val builder = StringBuilder(input)

        var lengthDiff = 0

        matched.forEach { group ->
            val replacement = getValue(group.value, visitor, null)

            replacement?.run {
                builder.replace(lengthDiff + group.start, lengthDiff + group.end, replacement)
                lengthDiff += replacement.length - (group.end - group.start)
            }
        }

        return builder.toString()
    }

    override fun findPlaceholders(matched: Collection<MatchedGroup>): List<Placeholder<out Any?>> {
        val result = mutableListOf<Placeholder<out Any?>>()

        matched.forEach {
            val found = placeholders[it.value]

            if (found != null) {
                result.add(found)
            }
        }

        return result
    }

    override fun getPlaceholder(key: String) = placeholders[key]

    override fun updatePlaceholder(key: String, visitor: Player?) {
        getPlaceholder(key)?.forceUpdate(visitor)
    }

    private fun updatePlaceholders() {
        this.updatePlaceholders.values.forEach {
            it.autoUpdate()
        }
    }

    override fun getPlaceholders() = HashMap(placeholders)

    private fun registerDefaultPlaceholders() {
        visitorSensitivePlaceholder("player", { p -> p.name }, -1, false, "playername")
        visitorSensitivePlaceholder("player_displayname", { p -> p.displayName }, 100)
        visitorSensitivePlaceholder("player_uuid", { p -> p.uniqueId })
        visitorSensitivePlaceholder("player_ping", { p -> p.ping })
        visitorSensitivePlaceholder("player_level", { p -> p.level?.name })
        visitorSensitivePlaceholder("player_can_fly", { p -> p.adventureSettings?.get(AdventureSettings.Type.ALLOW_FLIGHT) })
        visitorSensitivePlaceholder("player_flying", { p -> p.adventureSettings?.get(AdventureSettings.Type.FLYING) })
        visitorSensitivePlaceholder("player_health", { p -> p.health })
        visitorSensitivePlaceholder("player_max_health", { p -> p.maxHealth })
        visitorSensitivePlaceholder("player_saturation", { p -> p.foodData.foodSaturationLevel })
        visitorSensitivePlaceholder("player_food", { p -> p.foodData.level })
        visitorSensitivePlaceholder("player_gamemode", { p -> Server.getGamemodeString(p.gamemode, true) })
        visitorSensitivePlaceholder("player_x", { p -> p.x.round(configuration.coordsAccuracy) }, 0)
        visitorSensitivePlaceholder("player_y", { p -> p.y.round(configuration.coordsAccuracy) }, 0)
        visitorSensitivePlaceholder("player_direction", { p -> p.direction.getName() }, 10)
        visitorSensitivePlaceholder("player_exp", { p -> p.experience })
        visitorSensitivePlaceholder("player_exp_total", { p -> p.experience })
        visitorSensitivePlaceholder("player_z", { p -> p.z.round(configuration.coordsAccuracy) }, 0)
        visitorSensitivePlaceholder("player_exp_to_next", { p -> Player.calculateRequireExperience(p.experienceLevel + 1) })
        visitorSensitivePlaceholder("player_exp_level", { p -> p.experienceLevel })
        visitorSensitivePlaceholder("player_speed", { p -> p.movementSpeed })
        visitorSensitivePlaceholder("player_max_air", { p -> p.getDataProperty(Entity.DATA_MAX_AIR).data }, 100)
        visitorSensitivePlaceholder("player_remaining_air", { p -> p.getDataProperty(Entity.DATA_AIR).data }, 10)
        visitorSensitivePlaceholder("player_item_in_hand", { p -> p.inventory?.itemInHand?.name }, 10)

        val server = plugin.server
        val runtime = Runtime.getRuntime()

        staticPlaceholder("server_online", { server.onlinePlayers.size })
        staticPlaceholder("server_max_players", { server.maxPlayers })
        staticPlaceholder("server_motd", { server.network.name })
        staticPlaceholder("server_ram_used", { (runtime.totalMemory() - runtime.freeMemory()).bytes2MB() })
        staticPlaceholder("server_ram_free", { runtime.freeMemory().bytes2MB() })
        staticPlaceholder("server_ram_total", { runtime.totalMemory().bytes2MB() })
        staticPlaceholder("server_ram_max", { runtime.maxMemory().bytes2MB() })
        staticPlaceholder("server_cores", { runtime.availableProcessors() })
        staticPlaceholder("server_tps", { server.ticksPerSecondAverage })
        staticPlaceholder("server_uptime", { SimpleDateFormat((System.currentTimeMillis() - Nukkit.START_TIME).formatAsTime(configuration.timeFormat)) })


        staticPlaceholder("time", { SimpleDateFormat("${configuration.dateFormat} ${configuration.timeFormat}").format(Date()) }, 10)
    }
}