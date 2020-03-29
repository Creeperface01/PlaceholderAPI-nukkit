package com.creeperface.nukkit.placeholderapi

import cn.nukkit.AdventureSettings.Type
import cn.nukkit.Nukkit
import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.block.Block
import cn.nukkit.entity.Entity
import cn.nukkit.item.Item
import cn.nukkit.plugin.Plugin
import com.creeperface.nukkit.placeholderapi.api.PlaceholderParameters
import com.creeperface.nukkit.placeholderapi.api.event.PlaceholderAPIInitializeEvent
import com.creeperface.nukkit.placeholderapi.api.scope.GlobalScope
import com.creeperface.nukkit.placeholderapi.api.util.*
import com.creeperface.nukkit.placeholderapi.command.PlaceholderCommand
import com.creeperface.nukkit.placeholderapi.placeholder.StaticPlaceHolder
import com.creeperface.nukkit.placeholderapi.placeholder.VisitorSensitivePlaceholder
import com.creeperface.nukkit.placeholderapi.util.*
import com.google.common.base.Preconditions
import java.time.Duration
import java.util.*
import kotlin.jvm.internal.Ref
import kotlin.reflect.KClass
import com.creeperface.nukkit.placeholderapi.api.PlaceholderAPI as API

/**
 * @author CreeperFace
 */
@Suppress("DEPRECATION")
class PlaceholderAPIIml private constructor(plugin: PlaceholderPlugin) : API(), Plugin by plugin {

    override val globalScope = GlobalScope

    private val globalPlaceholders = mutableMapOf<String, AnyPlaceholder>()
    private val scopePlaceholders = mutableMapOf<AnyScopeClass, PlaceholderGroup>()

    private val updatePlaceholders = mutableMapOf<String, AnyPlaceholder>()

    private val formatters = mutableMapOf<KClass<*>, PFormatter>()

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

        registerDefaultFormatters()
    }

    internal fun init() {
        registerDefaultPlaceholders()

        this.server.scheduler.scheduleRepeatingTask(this, { updatePlaceholders() }, configuration.updateInterval)

        this.server.commandMap.register("placeholder", PlaceholderCommand())
        this.server.scheduler.scheduleTask(this) {
            this.server.pluginManager.callEvent(PlaceholderAPIInitializeEvent(this))
        }
    }

    override fun <T : Any> staticPlaceholder(
            name: String,
            typeClass: KClass<T>,
            loader: Loader<T>,
            updateInterval: Int,
            autoUpdate: Boolean,
            processParameters: Boolean,
            scope: AnyScopeClass,
            vararg aliases: String) {
        registerPlaceholder(
                StaticPlaceHolder(
                        name,
                        updateInterval,
                        autoUpdate,
                        aliases.toSet(),
                        processParameters,
                        scope,
                        typeClass,
                        getFormatter(typeClass),
                        loader
                )
        )
    }

    override fun <T : Any> visitorSensitivePlaceholder(
            name: String,
            typeClass: KClass<T>,
            loader: Loader<T>,
            updateInterval: Int,
            autoUpdate: Boolean,
            processParameters: Boolean,
            scope: AnyScopeClass,
            vararg aliases: String) {
        registerPlaceholder(
                VisitorSensitivePlaceholder(
                        name,
                        updateInterval,
                        autoUpdate,
                        aliases.toSet(),
                        processParameters,
                        scope,
                        typeClass,
                        getFormatter(typeClass),
                        loader
                )
        )
    }

    override fun registerPlaceholder(placeholder: AnyPlaceholder) {
        val group = this.scopePlaceholders.computeIfAbsent(placeholder.scope::class) { mutableMapOf() }
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

        //TODO: placeholder as a parameter (calculate nested placeholders)
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
            scopePlaceholders[current.scope::class]?.get(key)?.let {
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
            scopePlaceholders[scope::class]?.get(key)?.let {
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
            scopePlaceholders[it::class]?.let { group ->
                placeholders.putAll(group)
            }
        }

        return placeholders
    }

    override fun formatDate(millis: Long) = millis.formatAsTime("${configuration.dateFormat} ${configuration.timeFormat}")

    override fun formatTime(millis: Long) = millis.formatAsTime(configuration.timeFormat)

    override fun formatBoolean(value: Boolean) = value.toFormatString()

    override fun formatObject(value: Any?): String {
        if (value == null) {
            return "null"
        }

        return getFormatter(value::class)(value)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> registerFormatter(clazz: KClass<T>, formatFun: (T) -> String) {
        formatters[clazz] = format@{
            it?.let {
                formatFun(it as T)
            }

            return@format "null"
        }
    }

    override fun getFormatter(clazz: KClass<*>): PFormatter {
        var formatter: PFormatter? = null
        var currentLevel = Int.MAX_VALUE

        formatters.forEach { (formClazz, form) ->
            if (formClazz == clazz) {
                return form
            }

            val level = clazz.nestedSuperClass(formClazz)

            if (level in 0 until currentLevel) {
                currentLevel = level
                formatter = form
            }
        }

        return formatter ?: { it.toString() }
    }

    private fun registerDefaultFormatters() {
        registerFormatter(Boolean::class) { formatBoolean(it) }
        registerFormatter(Date::class) { formatDate(it) }
        registerFormatter(Iterable::class) { it.joinToString(configuration.arraySeparator) }
        registerFormatter(Array<Any?>::class) { it.joinToString(configuration.arraySeparator) }
        registerFormatter(Player::class) { it.name }
        registerFormatter(Item::class) { it.name }
        registerFormatter(Block::class) { it.name }
    }

    private fun registerDefaultPlaceholders() {
        build<String>("player") {
            visitorLoader { player.name }
            aliases("playername")
        }

        build<String>("player_displayname") {
            visitorLoader { player.displayName }
        }

        build<UUID>("player_uuid") {
            visitorLoader { player.uniqueId }
        }
        build<Int>("player_ping") {
            visitorLoader { player.ping }
        }
        build<String>("player_level") {
            visitorLoader { player.level?.name }
        }
        build<Boolean>("player_can_fly") {
            visitorLoader { player.adventureSettings.get(Type.ALLOW_FLIGHT) }
        }
        build<Boolean>("player_flying") {
            visitorLoader { player.adventureSettings.get(Type.FLYING) }
        }

        build<Float>("player_health") {
            visitorLoader { player.health }
        }
        build<Int>("player_max_health") {
            visitorLoader { player.maxHealth }
        }
        build<Float>("player_saturation") {
            visitorLoader { player.foodData.foodSaturationLevel }
        }
        build<Int>("player_food") {
            visitorLoader { player.foodData.level }
        }
        build<String>("player_gamemode") {
            visitorLoader { Server.getGamemodeString(player.gamemode) }
        }

        build<Double>("player_x") {
            visitorLoader { player.x }
        }
        build<Double>("player_y") {
            visitorLoader { player.y }
        }
        build<Double>("player_z") {
            visitorLoader { player.z }
        }
        build<String>("player_direction") {
            visitorLoader { player.direction.name }
        }
        build<Int>("player_exp") {
            visitorLoader { player.experience }
        }

        build<Int>("player_exp_to_next") {
            visitorLoader { Player.calculateRequireExperience(player.experienceLevel + 1) }
        }
        build<Int>("player_exp_level") {
            visitorLoader { player.experienceLevel }
        }
        build<Float>("player_speed") {
            visitorLoader { player.movementSpeed }
        }
        build<Int>("player_max_air") {
            visitorLoader { player.getDataPropertyInt(Entity.DATA_MAX_AIR) }
        }
        build<Int>("player_remaining_air") {
            visitorLoader { player.getDataPropertyInt(Entity.DATA_AIR) }
        }
        build<String>("player_item_in_hand") {
            visitorLoader { player.inventory?.itemInHand?.name }
        }

        val server = this.server
        val runtime = Runtime.getRuntime()

        build<Int>("server_online") {
            loader {
                server.onlinePlayers.size
            }
        }

        build<Int>("server_max_players") {
            loader {
                server.maxPlayers
            }
        }

        build<String>("server_motd") {
            loader {
                server.network.name
            }
        }

        build<Double>("server_ram_used") {
            loader {
                (runtime.totalMemory() - runtime.freeMemory()).bytes2MB().round(configuration.coordsAccuracy)
            }
        }

        build<Double>("server_ram_free") {
            loader {
                runtime.freeMemory().bytes2MB().round(configuration.coordsAccuracy)
            }
        }

        build<Double>("server_ram_total") {
            loader {
                runtime.totalMemory().bytes2MB().round(configuration.coordsAccuracy)
            }
        }

        build<Double>("server_ram_max") {
            loader {
                runtime.maxMemory().bytes2MB().round(configuration.coordsAccuracy)
            }
        }

        build<Int>("server_cores") {
            loader {
                runtime.availableProcessors()
            }
        }

        build<Float>("server_tps") {
            loader {
                server.ticksPerSecondAverage
            }
        }

        build<Duration>("server_uptime") {
            loader {
                Duration.ofMillis(System.currentTimeMillis() - Nukkit.START_TIME)
            }
        }

        build<Date>("time") {
            loader { Date() }
        }
    }
}