package com.creeperface.nukkit.placeholderapi

import cn.nukkit.AdventureSettings
import cn.nukkit.Nukkit
import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.entity.Entity
import cn.nukkit.plugin.Plugin
import com.creeperface.nukkit.placeholderapi.api.Placeholder
import com.creeperface.nukkit.placeholderapi.api.util.MatchedGroup
import com.creeperface.nukkit.placeholderapi.command.PlaceholderCommand
import com.creeperface.nukkit.placeholderapi.placeholder.StaticPlaceHolder
import com.creeperface.nukkit.placeholderapi.placeholder.VisitorSensitivePlaceholder
import com.creeperface.nukkit.placeholderapi.util.bytes2MB
import com.creeperface.nukkit.placeholderapi.util.formatAsTime
import com.creeperface.nukkit.placeholderapi.util.round
import com.google.common.base.Preconditions
import java.util.*
import java.util.function.BiFunction
import java.util.function.Function
import java.util.function.Supplier
import kotlin.collections.HashMap
import com.creeperface.nukkit.placeholderapi.api.PlaceholderAPI as API

/**
 * @author CreeperFace
 */
class PlaceholderAPIIml private constructor(plugin: PlaceholderPlugin) : API, Plugin by plugin {

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
            Preconditions.checkState(!initialized, "PlaceholderAPI has been already initialized")

            val instance = PlaceholderAPIIml(plugin)

            this.instance = instance
            initialized = true
            return instance
        }
    }

    init {
        registerDefaultPlaceholders()

        saveDefaultConfig()
        configuration = Configuration(this)
        configuration.load()
    }

    internal fun init() {
        this.server.scheduler.scheduleRepeatingTask(this, { updatePlaceholders() }, configuration.updateInterval)

        this.server.commandMap.register("placeholder", PlaceholderCommand())
    }

    override fun <T> staticPlaceholder(name: String, loader: Function<Map<String, String>, T?>, updateInterval: Int, autoUpdate: Boolean, vararg aliases: String) {
        registerPlaceholder(StaticPlaceHolder(name, updateInterval, autoUpdate, aliases.toSet(), false, loader))
    }

    override fun <T> visitorSensitivePlaceholder(name: String, loader: BiFunction<Player, Map<String, String>, T?>, updateInterval: Int, autoUpdate: Boolean, vararg aliases: String) {
        registerPlaceholder(VisitorSensitivePlaceholder(name, updateInterval, autoUpdate, aliases.toSet(), false, loader))
    }

    override fun registerPlaceholder(placeholder: Placeholder<out Any?>) {
        val existing = this.placeholders.putIfAbsent(placeholder.name, placeholder)
        Preconditions.checkState(existing != placeholder, "Trying to register placeholder '${placeholder.name}' which already exists")

        placeholder.aliases.forEach {
            val v = this.placeholders.putIfAbsent(it, placeholder)

            if (v != null && v != placeholder) {
                this.logger.warning("Placeholder '${placeholder.name}' tried to register alias '$it' which is already used by a hologram '${v.name}'")
            }
        }

        if (placeholder.updateInterval > 0 && placeholder.autoUpdate) {
            updatePlaceholders[placeholder.name] = placeholder
        }
    }

    override fun getValue(key: String, visitor: Player?, defaultValue: String?, params: Map<String, String>): String? =
            placeholders[key]?.getValue(params, visitor) ?: key

    override fun translateString(input: String, visitor: Player?, matched: Collection<MatchedGroup>): String {
        val builder = StringBuilder(input)

        var lengthDiff = 0

        matched.forEach { group ->
            val replacement = getValue(group.value, visitor, null, group.params)

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
        getPlaceholder(key)?.forceUpdate(player = visitor)
    }

    private fun updatePlaceholders() {
        this.updatePlaceholders.values.forEach {
            it.autoUpdate()
        }
    }

    override fun getPlaceholders() = HashMap(placeholders)

    private fun registerDefaultPlaceholders() {
        visitorSensitivePlaceholder("test", BiFunction<Player, Map<String, String>, String?> { p, args ->
            "Hi ${p.name}, ${args["message"] ?: ""}"
        })

        visitorSensitivePlaceholder("player", BiFunction<Player, Map<String, String>, String?> { p, _ -> p.name }, "playername")
        visitorSensitivePlaceholder("player_displayname", BiFunction<Player, Map<String, String>, String?> { p, _ -> p.displayName })
        visitorSensitivePlaceholder("player_uuid", BiFunction<Player, Map<String, String>, UUID?> { p, _ -> p.uniqueId })
        visitorSensitivePlaceholder("player_ping", BiFunction<Player, Map<String, String>, Int?> { p, _ -> p.ping })
        visitorSensitivePlaceholder("player_level", BiFunction<Player, Map<String, String>, String?> { p, _ -> p.level?.name })
        visitorSensitivePlaceholder("player_can_fly", BiFunction<Player, Map<String, String>, Boolean?> { p, _ -> p.adventureSettings?.get(AdventureSettings.Type.ALLOW_FLIGHT) })
        visitorSensitivePlaceholder("player_flying", BiFunction<Player, Map<String, String>, Boolean?> { p, _ -> p.adventureSettings?.get(AdventureSettings.Type.FLYING) })
        visitorSensitivePlaceholder("player_health", BiFunction<Player, Map<String, String>, Float?> { p, _ -> p.health })
        visitorSensitivePlaceholder("player_max_health", BiFunction<Player, Map<String, String>, Int?> { p, _ -> p.maxHealth })
        visitorSensitivePlaceholder("player_saturation", BiFunction<Player, Map<String, String>, Float?> { p, _ -> p.foodData.foodSaturationLevel })
        visitorSensitivePlaceholder("player_food", BiFunction<Player, Map<String, String>, Int?> { p, _ -> p.foodData.level })
        visitorSensitivePlaceholder("player_gamemode", BiFunction<Player, Map<String, String>, String?> { p, _ -> Server.getGamemodeString(p.gamemode, true) })
        visitorSensitivePlaceholder("player_x", BiFunction<Player, Map<String, String>, Double?> { p, _ -> p.x.round(configuration.coordsAccuracy) }, 0)
        visitorSensitivePlaceholder("player_y", BiFunction<Player, Map<String, String>, Double?> { p, _ -> p.y.round(configuration.coordsAccuracy) }, 0)
        visitorSensitivePlaceholder("player_direction", BiFunction<Player, Map<String, String>, String?> { p, _ -> p.direction.getName() }, 10)
        visitorSensitivePlaceholder("player_exp", BiFunction<Player, Map<String, String>, Int?> { p, _ -> p.experience }, "player_exp_total")
        visitorSensitivePlaceholder("", BiFunction<Player, Map<String, String>, Int?> { p, _ -> p.experience })
        visitorSensitivePlaceholder("player_z", BiFunction<Player, Map<String, String>, Double?> { p, _ -> p.z.round(configuration.coordsAccuracy) }, 0)
        visitorSensitivePlaceholder("player_exp_to_next", BiFunction<Player, Map<String, String>, Int?> { p, _ -> Player.calculateRequireExperience(p.experienceLevel + 1) })
        visitorSensitivePlaceholder("player_exp_level", BiFunction<Player, Map<String, String>, Int?> { p, _ -> p.experienceLevel })
        visitorSensitivePlaceholder("player_speed", BiFunction<Player, Map<String, String>, Float?> { p, _ -> p.movementSpeed })
        visitorSensitivePlaceholder("player_max_air", BiFunction<Player, Map<String, String>, Int?> { p, _ -> p.getDataPropertyInt(Entity.DATA_MAX_AIR) }, 100)
        visitorSensitivePlaceholder("player_remaining_air", BiFunction<Player, Map<String, String>, Int?> { p, _ -> p.getDataPropertyInt(Entity.DATA_AIR) }, 10)
        visitorSensitivePlaceholder("player_item_in_hand", BiFunction<Player, Map<String, String>, String?> { p, _ -> p.inventory?.itemInHand?.name }, 10)

        visitorSensitivePlaceholder("player_x", BiFunction<Player, Map<String, String>, Double?> { p, _ -> p.x.round(configuration.coordsAccuracy) }, 0)

        val server = this.server
        val runtime = Runtime.getRuntime()

        staticPlaceholder("server_online", Function<Map<String, String>, Int?> { server.onlinePlayers.size })
        staticPlaceholder("server_max_players", Function<Map<String, String>, Int?> { server.maxPlayers })
        staticPlaceholder("server_motd", Function<Map<String, String>, String?> { server.network.name })
        staticPlaceholder("server_ram_used", Function<Map<String, String>, Double?> { (runtime.totalMemory() - runtime.freeMemory()).bytes2MB().round(configuration.coordsAccuracy) })
        staticPlaceholder("server_ram_free", Function<Map<String, String>, Double?> { runtime.freeMemory().bytes2MB().round(configuration.coordsAccuracy) })
        staticPlaceholder("server_ram_total", Function<Map<String, String>, Double?> { runtime.totalMemory().bytes2MB().round(configuration.coordsAccuracy) })
        staticPlaceholder("server_ram_max", Function<Map<String, String>, Double?> { runtime.maxMemory().bytes2MB().round(configuration.coordsAccuracy) })
        staticPlaceholder("server_cores", Function<Map<String, String>, Int?> { runtime.availableProcessors() })
        staticPlaceholder("server_tps", Function<Map<String, String>, Float?> { server.ticksPerSecondAverage })
        staticPlaceholder("server_uptime", Function<Map<String, String>, String?> { (System.currentTimeMillis() - Nukkit.START_TIME).formatAsTime(configuration.timeFormat) })

        staticPlaceholder("time", Supplier<String?> { System.currentTimeMillis().formatAsTime("${configuration.dateFormat} ${configuration.timeFormat}") }, 10)
    }
}