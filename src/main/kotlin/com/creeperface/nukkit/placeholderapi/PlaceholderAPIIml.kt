package com.creeperface.nukkit.placeholderapi

import cn.nukkit.AdventureSettings
import cn.nukkit.Nukkit
import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.entity.Entity
import cn.nukkit.plugin.Plugin
import com.creeperface.nukkit.placeholderapi.api.PlaceholderParameters
import com.creeperface.nukkit.placeholderapi.api.scope.GlobalScope
import com.creeperface.nukkit.placeholderapi.api.util.*
import com.creeperface.nukkit.placeholderapi.command.PlaceholderCommand
import com.creeperface.nukkit.placeholderapi.placeholder.StaticPlaceHolder
import com.creeperface.nukkit.placeholderapi.placeholder.VisitorSensitivePlaceholder
import com.creeperface.nukkit.placeholderapi.util.bytes2MB
import com.creeperface.nukkit.placeholderapi.util.formatAsTime
import com.creeperface.nukkit.placeholderapi.util.round
import com.creeperface.nukkit.placeholderapi.util.toFormatString
import com.google.common.base.Preconditions
import java.util.*
import java.util.function.BiFunction
import java.util.function.Function
import kotlin.jvm.internal.Ref
import com.creeperface.nukkit.placeholderapi.api.PlaceholderAPI as API

/**
 * @author CreeperFace
 */
class PlaceholderAPIIml private constructor(plugin: PlaceholderPlugin) : API, Plugin by plugin {

    override val globalScope = GlobalScope

    private val globalPlaceholders = mutableMapOf<String, AnyPlaceholder>()
    private val scopePlaceholders = mutableMapOf<AnyScope, PlaceholderGroup>()

    private val updatePlaceholders = mutableMapOf<String, AnyPlaceholder>()

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
        saveDefaultConfig()
        configuration = Configuration(this)
        configuration.load()
    }

    internal fun init() {
        registerDefaultPlaceholders()

        this.server.scheduler.scheduleRepeatingTask(this, { updatePlaceholders() }, configuration.updateInterval)

        this.server.commandMap.register("placeholder", PlaceholderCommand())
    }

    override fun <T> staticPlaceholder(
            name: String,
            loader: (PlaceholderParameters, AnyContext) -> T?,
            updateInterval: Int,
            autoUpdate: Boolean,
            processParameters: Boolean,
            scope: AnyScope,
            vararg aliases: String) where T : Any? {
        registerPlaceholder(
                StaticPlaceHolder(
                        name,
                        updateInterval,
                        autoUpdate,
                        aliases.toSet(),
                        processParameters,
                        scope,
                        loader
                )
        )
    }

    override fun <T> visitorSensitivePlaceholder(
            name: String,
            loader: (Player, PlaceholderParameters, AnyContext) -> T?,
            updateInterval: Int,
            autoUpdate: Boolean,
            processParameters: Boolean,
            scope: AnyScope,
            vararg aliases: String) where T : Any? {
        registerPlaceholder(
                VisitorSensitivePlaceholder(
                        name,
                        updateInterval,
                        autoUpdate,
                        aliases.toSet(),
                        processParameters,
                        scope,
                        loader
                )
        )
    }

    override fun registerPlaceholder(placeholder: AnyPlaceholder) {
        val group = this.scopePlaceholders.computeIfAbsent(placeholder.scope) { mutableMapOf() }
        val existing = group.putIfAbsent(placeholder.name, placeholder)

        require(existing == null) { "Trying to register placeholder '${placeholder.name}' which already exists" }

        if (placeholder.scope.global) {
            globalPlaceholders[placeholder.name] = placeholder
        }

        placeholder.aliases.forEach {
            val v = group.putIfAbsent(it, placeholder)

            if (v != null && v != placeholder) {
                this.logger.warning("Placeholder '${placeholder.name}' tried to register alias '$it' which is already used by a placeholder '${v.name}'")
            }
        }

        if (placeholder.updateInterval > 0 && placeholder.autoUpdate) {
            updatePlaceholders[placeholder.name] = placeholder
        }
    }

    override fun getValue(key: String, visitor: Player?, defaultValue: String?, params: PlaceholderParameters, context: AnyContext): String? {
        val ref = Ref.ObjectRef<AnyContext>()

        getPlaceholder(key, context, ref)?.let {
            return it.getValue(params, ref.element, visitor)
        }

        return key
    }


    override fun translateString(input: String, visitor: Player?, context: AnyContext, matched: Collection<MatchedGroup>): String {
        val builder = StringBuilder(input)

        var lengthDiff = 0

        matched.forEach { group ->
            val replacement = getValue(group.value, visitor, null, group.params, context)

            replacement?.run {
                builder.replace(lengthDiff + group.start, lengthDiff + group.end, replacement)
                lengthDiff += replacement.length - (group.end - group.start)
            }
        }

        return builder.toString()
    }

    override fun findPlaceholders(matched: Collection<MatchedGroup>, scope: AnyScope): List<AnyPlaceholder> {
        val result = mutableListOf<AnyPlaceholder>()

        matched.forEach {
            getPlaceholder(it.value, scope)?.let { found ->
                result.add(found)
            }
        }

        return result
    }

    private fun getPlaceholder(key: String, context: AnyContext, placeholderContext: Ref.ObjectRef<AnyContext>): AnyPlaceholder? {
        if (context.scope.global) {
            placeholderContext.element = GlobalScope.defaultContext
            return globalPlaceholders[key]
        }

        var current = context

        while (true) {
            scopePlaceholders[current.scope]?.get(key)?.let {
                placeholderContext.element = current
                return it
            }

            current = current.parentContext ?: break
        }

        return null
    }

    override fun getPlaceholder(key: String, scope: AnyScope): AnyPlaceholder? {
        if (scope.global) {
            return globalPlaceholders[key]
        }

        var current = scope

        while (true) {
            scopePlaceholders[scope]?.get(key)?.let {
                return it
            }

            current = current.parent ?: break
        }

        return null
    }

    override fun updatePlaceholder(key: String, visitor: Player?, context: AnyContext) {
        getPlaceholder(key)?.forceUpdate(player = visitor, context = context)
    }

    private fun updatePlaceholders() {
        this.updatePlaceholders.values.forEach {
            it.autoUpdate()
        }
    }

    override fun getPlaceholders(scope: AnyScope): PlaceholderGroup {
        if (scope.global) {
            return globalPlaceholders
        }

        val scopes = mutableListOf<AnyScope>()

        while (true) {
            scopes.add(scope.parent ?: break)
        }

        val placeholders = mutableMapOf<String, AnyPlaceholder>()
        scopes.reversed().forEach {
            scopePlaceholders[it]?.let { group ->
                placeholders.putAll(group)
            }
        }

        return placeholders
    }

    override fun formatDate(millis: Long) = millis.formatAsTime("${configuration.dateFormat} ${configuration.timeFormat}")

    override fun formatTime(millis: Long) = millis.formatAsTime(configuration.timeFormat)

    override fun formatBoolean(value: Boolean) = value.toFormatString()

    private fun registerDefaultPlaceholders() {
        buildVisitorSensitive<String>("player", BiFunction { p, _ -> p.name }).aliases("playername").build()
        buildVisitorSensitive<String>("player_displayname", BiFunction { p, _ -> p.displayName }).aliases().build()
        buildVisitorSensitive<UUID>("player_uuid", BiFunction { p, _ -> p.uniqueId }).aliases().build()
        buildVisitorSensitive<Int>("player_ping", BiFunction { p, _ -> p.ping }).aliases().build()
        buildVisitorSensitive<String>("player_level", BiFunction { p, _ -> p.level?.name }).aliases().build()
        buildVisitorSensitive<Boolean>("player_can_fly", BiFunction { p, _ -> p.adventureSettings?.get(AdventureSettings.Type.ALLOW_FLIGHT) }).aliases().build()
        buildVisitorSensitive<Boolean>("player_flying", BiFunction { p, _ -> p.adventureSettings?.get(AdventureSettings.Type.FLYING) }).aliases().build()
        buildVisitorSensitive<Float>("player_health", BiFunction { p, _ -> p.health }).aliases().build()
        buildVisitorSensitive<Int>("player_max_health", BiFunction { p, _ -> p.maxHealth }).aliases().build()
        buildVisitorSensitive<Float>("player_saturation", BiFunction { p, _ -> p.foodData.foodSaturationLevel }).aliases().build()
        buildVisitorSensitive<Int>("player_food", BiFunction { p, _ -> p.foodData.level }).aliases().build()
        buildVisitorSensitive<String>("player_gamemode", BiFunction { p, _ -> Server.getGamemodeString(p.gamemode, true) }).aliases().build()
        buildVisitorSensitive<Double>("player_x", BiFunction { p, _ -> p.x.round(configuration.coordsAccuracy) }).updateInterval(0).build()
        buildVisitorSensitive<Double>("player_y", BiFunction { p, _ -> p.y.round(configuration.coordsAccuracy) }).updateInterval(0).build()
        buildVisitorSensitive<Double>("player_z", BiFunction { p, _ -> p.z.round(configuration.coordsAccuracy) }).updateInterval(0).build()
        buildVisitorSensitive<String>("player_direction", BiFunction { p, _ -> p.direction.getName() }).updateInterval(10).build()
        buildVisitorSensitive<Int>("player_exp", BiFunction { p, _ -> p.experience }).aliases("player_exp_total").build()
        buildVisitorSensitive<Int>("player_exp_to_next", BiFunction { p, _ -> Player.calculateRequireExperience(p.experienceLevel + 1) }).aliases().build()
        buildVisitorSensitive<Int>("player_exp_level", BiFunction { p, _ -> p.experienceLevel }).aliases().build()
        buildVisitorSensitive<Float>("player_speed", BiFunction { p, _ -> p.movementSpeed }).aliases().build()
        buildVisitorSensitive<Int>("player_max_air", BiFunction { p, _ -> p.getDataPropertyInt(Entity.DATA_MAX_AIR) }).updateInterval(100).build()
        buildVisitorSensitive<Int>("player_remaining_air", BiFunction { p, _ -> p.getDataPropertyInt(Entity.DATA_AIR) }).updateInterval(10).build()
        buildVisitorSensitive<String?>("player_item_in_hand", BiFunction { p, _ -> p.inventory?.itemInHand?.name }).updateInterval(10).build()

        val server = this.server
        val runtime = Runtime.getRuntime()

        buildStatic<Int>("server_online", Function { server.onlinePlayers.size }).aliases().build()
        buildStatic<Int>("server_max_players", Function { server.maxPlayers }).aliases().build()
        buildStatic<String>("server_motd", Function { server.network.name }).aliases().build()
        buildStatic<Double>("server_ram_used", Function { (runtime.totalMemory() - runtime.freeMemory()).bytes2MB().round(configuration.coordsAccuracy) }).aliases().build()
        buildStatic<Double>("server_ram_free", Function { runtime.freeMemory().bytes2MB().round(configuration.coordsAccuracy) }).aliases().build()
        buildStatic<Double>("server_ram_total", Function { runtime.totalMemory().bytes2MB().round(configuration.coordsAccuracy) }).aliases().build()
        buildStatic<Double>("server_ram_max", Function { runtime.maxMemory().bytes2MB().round(configuration.coordsAccuracy) }).aliases().build()
        buildStatic<Int>("server_cores", Function { runtime.availableProcessors() }).aliases().build()
        buildStatic<Float>("server_tps", Function { server.ticksPerSecondAverage }).aliases().build()
        buildStatic<String>("server_uptime", Function { (System.currentTimeMillis() - Nukkit.START_TIME).formatAsTime(configuration.timeFormat) }).aliases().build()

        buildStatic<String>("time", Function { formatTime(System.currentTimeMillis()) }).updateInterval(10).build()
    }
}