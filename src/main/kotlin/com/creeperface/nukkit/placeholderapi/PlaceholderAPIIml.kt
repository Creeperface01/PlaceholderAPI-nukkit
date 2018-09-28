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
import java.util.*
import java.util.function.Function
import java.util.function.Supplier
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
        fun createInstance(plugin: PlaceholderPlugin): PlaceholderAPIIml {
            Preconditions.checkState(!initialized, "PlaceholderAPI was already initialized")

            val instance = PlaceholderAPIIml(plugin)

            this.instance = instance
            initialized = true
            return instance
        }
    }

    init {
        registerDefaultPlaceholders()

        plugin.saveDefaultConfig()
        configuration = Configuration(plugin)
        configuration.load()
    }

    internal fun init() {
        plugin.server.pluginManager.registerEvents(this, plugin)

        plugin.server.scheduler.scheduleRepeatingTask(plugin, { updatePlaceholders() }, configuration.updateInterval)
    }

    override fun <T> staticPlaceholder(name: String, loader: Supplier<T?>, vararg aliases: String) {
        staticPlaceholder(name, loader, 20, false, *aliases)
    }

    override fun <T> staticPlaceholder(name: String, loader: Supplier<T?>, updateInterval: Int, autoUpdate: Boolean, vararg aliases: String) {
        registerPlaceholder(StaticPlaceHolder(name, updateInterval, autoUpdate, aliases.toSet(), loader))
    }

    override fun <T> visitorSensitivePlaceholder(name: String, loader: Function<Player, T?>, vararg aliases: String) {
        visitorSensitivePlaceholder(name, loader, 20, false, *aliases)
    }

    override fun <T> visitorSensitivePlaceholder(name: String, loader: Function<Player, T?>, updateInterval: Int, autoUpdate: Boolean, vararg aliases: String) {
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
        visitorSensitivePlaceholder("player", Function<Player, String?> { p -> p.name }, "playername")
        visitorSensitivePlaceholder("player_displayname", Function<Player, String?> { p -> p.displayName })
        visitorSensitivePlaceholder("player_uuid", Function<Player, UUID?> { p -> p.uniqueId })
        visitorSensitivePlaceholder("player_ping", Function<Player, Int?> { p -> p.ping })
        visitorSensitivePlaceholder("player_level", Function<Player, String?> { p -> p.level?.name })
        visitorSensitivePlaceholder("player_can_fly", Function<Player, Boolean?> { p -> p.adventureSettings?.get(AdventureSettings.Type.ALLOW_FLIGHT) })
        visitorSensitivePlaceholder("player_flying", Function<Player, Boolean?> { p -> p.adventureSettings?.get(AdventureSettings.Type.FLYING) })
        visitorSensitivePlaceholder("player_health", Function<Player, Float?> { p -> p.health })
        visitorSensitivePlaceholder("player_max_health", Function<Player, Int?> { p -> p.maxHealth })
        visitorSensitivePlaceholder("player_saturation", Function<Player, Float?> { p -> p.foodData.foodSaturationLevel })
        visitorSensitivePlaceholder("player_food", Function<Player, Int?> { p -> p.foodData.level })
        visitorSensitivePlaceholder("player_gamemode", Function<Player, String?> { p -> Server.getGamemodeString(p.gamemode, true) })
        visitorSensitivePlaceholder("player_x", Function<Player, Double?> { p -> p.x.round(configuration.coordsAccuracy) }, 0)
        visitorSensitivePlaceholder("player_y", Function<Player, Double?> { p -> p.y.round(configuration.coordsAccuracy) }, 0)
        visitorSensitivePlaceholder("player_direction", Function<Player, String?> { p -> p.direction.getName() }, 10)
        visitorSensitivePlaceholder("player_exp", Function<Player, Int?> { p -> p.experience }, "player_exp_total")
        visitorSensitivePlaceholder("", Function<Player, Int?> { p -> p.experience })
        visitorSensitivePlaceholder("player_z", Function<Player, Double?> { p -> p.z.round(configuration.coordsAccuracy) }, 0)
        visitorSensitivePlaceholder("player_exp_to_next", Function<Player, Int?> { p -> Player.calculateRequireExperience(p.experienceLevel + 1) })
        visitorSensitivePlaceholder("player_exp_level", Function<Player, Int?> { p -> p.experienceLevel })
        visitorSensitivePlaceholder("player_speed", Function<Player, Float?> { p -> p.movementSpeed })
        visitorSensitivePlaceholder("player_max_air", Function<Player, Int?> { p -> p.getDataPropertyInt(Entity.DATA_MAX_AIR) }, 100)
        visitorSensitivePlaceholder("player_remaining_air", Function<Player, Int?> { p -> p.getDataPropertyInt(Entity.DATA_AIR) }, 10)
        visitorSensitivePlaceholder("player_item_in_hand", Function<Player, String?> { p -> p.inventory?.itemInHand?.name }, 10)

        visitorSensitivePlaceholder("player_x", Function<Player, Double?> { it.x.round(configuration.coordsAccuracy) }, 0)

        val server = plugin.server
        val runtime = Runtime.getRuntime()

        staticPlaceholder("server_online", Supplier<Int?> { server.onlinePlayers.size })
        staticPlaceholder("server_max_players", Supplier<Int?> { server.maxPlayers })
        staticPlaceholder("server_motd", Supplier<String?> { server.network.name })
        staticPlaceholder("server_ram_used", Supplier<Double?> { (runtime.totalMemory() - runtime.freeMemory()).bytes2MB().round(configuration.coordsAccuracy) })
        staticPlaceholder("server_ram_free", Supplier<Double?> { runtime.freeMemory().bytes2MB().round(configuration.coordsAccuracy) })
        staticPlaceholder("server_ram_total", Supplier<Double?> { runtime.totalMemory().bytes2MB().round(configuration.coordsAccuracy) })
        staticPlaceholder("server_ram_max", Supplier<Double?> { runtime.maxMemory().bytes2MB().round(configuration.coordsAccuracy) })
        staticPlaceholder("server_cores", Supplier<Int?> { runtime.availableProcessors() })
        staticPlaceholder("server_tps", Supplier<Float?> { server.ticksPerSecondAverage })
        staticPlaceholder("server_uptime", Supplier<String?> { (System.currentTimeMillis() - Nukkit.START_TIME).formatAsTime(configuration.timeFormat) })

        staticPlaceholder("time", Supplier<String?> { System.currentTimeMillis().formatAsTime("${configuration.dateFormat} ${configuration.timeFormat}") }, 10)
    }
}