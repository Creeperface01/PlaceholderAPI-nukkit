package com.creeperface.nukkit.placeholderapi

import cn.nukkit.AdventureSettings
import cn.nukkit.Nukkit
import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.entity.Entity
import cn.nukkit.plugin.Plugin
import com.creeperface.nukkit.placeholderapi.api.Placeholder
import com.creeperface.nukkit.placeholderapi.api.PlaceholderParameters
import com.creeperface.nukkit.placeholderapi.api.util.MatchedGroup
import com.creeperface.nukkit.placeholderapi.command.PlaceholderCommand
import com.creeperface.nukkit.placeholderapi.placeholder.StaticPlaceHolder
import com.creeperface.nukkit.placeholderapi.placeholder.VisitorSensitivePlaceholder
import com.creeperface.nukkit.placeholderapi.util.*
import com.google.common.base.Preconditions
import java.util.*
import java.util.function.BiFunction
import java.util.function.Function
import kotlin.collections.HashMap
import com.creeperface.nukkit.placeholderapi.api.PlaceholderAPI as API

/**
 * @author CreeperFace
 */
class PlaceholderAPIIml private constructor(plugin: PlaceholderPlugin) : API, Plugin by plugin {

    private val placeholders = mutableMapOf<String, Placeholder<out Any?>>()
    private val updatePlaceholders = mutableMapOf<String, Placeholder<out Any?>>()

    val configuration: Configuration

    companion object {

        @JvmStatic
        lateinit var instance: PlaceholderAPIIml
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

    override fun <T> staticPlaceholder(name: String, loader: Function<PlaceholderParameters, T?>, updateInterval: Int, autoUpdate: Boolean, vararg aliases: String) where T : Any? {
        registerPlaceholder(StaticPlaceHolder(name, updateInterval, autoUpdate, aliases.toSet(), false, loader))
    }

    override fun <T> visitorSensitivePlaceholder(name: String, loader: BiFunction<Player, PlaceholderParameters, T?>, updateInterval: Int, autoUpdate: Boolean, vararg aliases: String) where T : Any? {
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

    override fun getValue(key: String, visitor: Player?, defaultValue: String?, params: PlaceholderParameters): String? =
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

    override fun formatDate(millis: Long) = millis.formatAsTime("${configuration.dateFormat} ${configuration.timeFormat}")

    override fun formatTime(millis: Long) = millis.formatAsTime(configuration.timeFormat)

    override fun formatBoolean(value: Boolean) = value.toFormatString()

    private fun registerDefaultPlaceholders() {
        visitorSensitivePlaceholder<String>("player", BiFunction { p, _ -> p.name }, "playername")
        visitorSensitivePlaceholder<String>("player_displayname", BiFunction { p, _ -> p.displayName })
        visitorSensitivePlaceholder<UUID>("player_uuid", BiFunction { p, _ -> p.uniqueId })
        visitorSensitivePlaceholder<Int>("player_ping", BiFunction { p, _ -> p.ping })
        visitorSensitivePlaceholder<String?>("player_level", BiFunction { p, _ -> p.level?.name })
        visitorSensitivePlaceholder<Boolean?>("player_can_fly", BiFunction { p, _ -> p.adventureSettings?.get(AdventureSettings.Type.ALLOW_FLIGHT) })
        visitorSensitivePlaceholder<Boolean?>("player_flying", BiFunction { p, _ -> p.adventureSettings?.get(AdventureSettings.Type.FLYING) })
        visitorSensitivePlaceholder<Float>("player_health", BiFunction { p, _ -> p.health })
        visitorSensitivePlaceholder<Int>("player_max_health", BiFunction { p, _ -> p.maxHealth })
        visitorSensitivePlaceholder<Float>("player_saturation", BiFunction { p, _ -> p.foodData.foodSaturationLevel })
        visitorSensitivePlaceholder<Int>("player_food", BiFunction { p, _ -> p.foodData.level })
        visitorSensitivePlaceholder<String?>("player_gamemode", BiFunction { p, _ -> Server.getGamemodeString(p.gamemode, true) })
        visitorSensitivePlaceholder<Double>("player_x", BiFunction { p, _ -> p.x.round(configuration.coordsAccuracy) }, 0)
        visitorSensitivePlaceholder<Double>("player_y", BiFunction { p, _ -> p.y.round(configuration.coordsAccuracy) }, 0)
        visitorSensitivePlaceholder<Double>("player_z", BiFunction { p, _ -> p.z.round(configuration.coordsAccuracy) }, 0)
        visitorSensitivePlaceholder<String>("player_direction", BiFunction { p, _ -> p.direction.getName() }, 10)
        visitorSensitivePlaceholder<Int>("player_exp", BiFunction { p, _ -> p.experience }, "player_exp_total")
        visitorSensitivePlaceholder<Int>("", BiFunction { p, _ -> p.experience })
        visitorSensitivePlaceholder<Int>("player_exp_to_next", BiFunction { p, _ -> Player.calculateRequireExperience(p.experienceLevel + 1) })
        visitorSensitivePlaceholder<Int>("player_exp_level", BiFunction { p, _ -> p.experienceLevel })
        visitorSensitivePlaceholder<Float>("player_speed", BiFunction { p, _ -> p.movementSpeed })
        visitorSensitivePlaceholder<Int>("player_max_air", BiFunction { p, _ -> p.getDataPropertyInt(Entity.DATA_MAX_AIR) }, 100)
        visitorSensitivePlaceholder<Int>("player_remaining_air", BiFunction { p, _ -> p.getDataPropertyInt(Entity.DATA_AIR) }, 10)
        visitorSensitivePlaceholder<String?>("player_item_in_hand", BiFunction { p, _ -> p.inventory?.itemInHand?.name }, 10)

        val server = this.server
        val runtime = Runtime.getRuntime()

        staticPlaceholder<Int>("server_online", Function { server.onlinePlayers.size })
        staticPlaceholder<Int>("server_max_players", Function { server.maxPlayers })
        staticPlaceholder<String>("server_motd", Function { server.network.name })
        staticPlaceholder<Double>("server_ram_used", Function { (runtime.totalMemory() - runtime.freeMemory()).bytes2MB().round(configuration.coordsAccuracy) })
        staticPlaceholder<Double>("server_ram_free", Function { runtime.freeMemory().bytes2MB().round(configuration.coordsAccuracy) })
        staticPlaceholder<Double>("server_ram_total", Function { runtime.totalMemory().bytes2MB().round(configuration.coordsAccuracy) })
        staticPlaceholder<Double>("server_ram_max", Function { runtime.maxMemory().bytes2MB().round(configuration.coordsAccuracy) })
        staticPlaceholder<Int>("server_cores", Function { runtime.availableProcessors() })
        staticPlaceholder<Float>("server_tps", Function { server.ticksPerSecondAverage })
        staticPlaceholder<String>("server_uptime", Function { (System.currentTimeMillis() - Nukkit.START_TIME).formatAsTime(configuration.timeFormat) })

        staticPlaceholder<String>("time", Function { formatTime(System.currentTimeMillis()) }, 10)
    }

    private fun optimisePlaceholders() { //TODO: finish later
        val minGroupLength = 3

        val groups = mutableMapOf<String, PlaceholderGroup>()

        fun createGroups(placeholders: Map<String, Placeholder<Any>>): Map<String, PlaceholderGroup> {
            val grps = mutableMapOf<String, PlaceholderGroup>()

            for ((name, placeholder) in placeholders) {
                val similar = mutableMapOf<String, Placeholder<Any>>()
                val prefix = name.substring(0, minGroupLength)

                for ((name_, placeholder_) in placeholders) {
                    if (name_.startsWith(prefix)) {
                        similar[name_.substring(minGroupLength)] = placeholder_
                    }
                }

                if (similar.size > minGroupLength) {
                    grps[prefix] = PlaceholderGroup(prefix, similar)
                }
            }

            return grps
        }

        val depth = 0

        while (true) {
            var groupMap = groups

            for (i in 0..depth) {

            }
        }
    }
}