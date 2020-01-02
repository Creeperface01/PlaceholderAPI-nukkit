package com.creeperface.nukkit.placeholderapi

import cn.nukkit.AdventureSettings
import cn.nukkit.Nukkit
import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.block.Block
import cn.nukkit.entity.Entity
import cn.nukkit.item.Item
import cn.nukkit.plugin.Plugin
import com.creeperface.nukkit.placeholderapi.api.PlaceholderParameters
import com.creeperface.nukkit.placeholderapi.api.scope.GlobalScope
import com.creeperface.nukkit.placeholderapi.api.util.*
import com.creeperface.nukkit.placeholderapi.command.PlaceholderCommand
import com.creeperface.nukkit.placeholderapi.placeholder.StaticPlaceHolder
import com.creeperface.nukkit.placeholderapi.placeholder.VisitorSensitivePlaceholder
import com.creeperface.nukkit.placeholderapi.util.*
import com.google.common.base.Preconditions
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
    }

    override fun <T : Any> staticPlaceholder(
            name: String,
            typeClass: KClass<T>,
            loader: (PlaceholderParameters, AnyContext) -> T?,
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
            loader: (Player, PlaceholderParameters, AnyContext) -> T?,
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
        buildVisitorSensitive("player") { p, _ -> p.name }.aliases("playername").build()
        buildVisitorSensitive("player_displayname") { p, _ -> p.displayName }.aliases().build()
        buildVisitorSensitive("player_uuid") { p, _ -> p.uniqueId }.aliases().build()
        buildVisitorSensitive("player_ping") { p, _ -> p.ping }.aliases().build()
        buildVisitorSensitive("player_level") { p, _ -> p.level?.name }.aliases().build()
        buildVisitorSensitive("player_can_fly") { p, _ -> p.adventureSettings?.get(AdventureSettings.Type.ALLOW_FLIGHT) }.aliases().build()
        buildVisitorSensitive("player_flying") { p, _ -> p.adventureSettings?.get(AdventureSettings.Type.FLYING) }.aliases().build()
        buildVisitorSensitive("player_health") { p, _ -> p.health }.aliases().build()
        buildVisitorSensitive("player_max_health") { p, _ -> p.maxHealth }.aliases().build()
        buildVisitorSensitive("player_saturation") { p, _ -> p.foodData.foodSaturationLevel }.aliases().build()
        buildVisitorSensitive("player_food") { p, _ -> p.foodData.level }.aliases().build()
        buildVisitorSensitive("player_gamemode") { p, _ -> Server.getGamemodeString(p.gamemode, true) }.aliases().build()
        buildVisitorSensitive("player_x") { p, _ -> p.x.round(configuration.coordsAccuracy) }.updateInterval(0).build()
        buildVisitorSensitive("player_y") { p, _ -> p.y.round(configuration.coordsAccuracy) }.updateInterval(0).build()
        buildVisitorSensitive("player_z") { p, _ -> p.z.round(configuration.coordsAccuracy) }.updateInterval(0).build()
        buildVisitorSensitive("player_direction") { p, _ -> p.direction.getName() }.updateInterval(10).build()
        buildVisitorSensitive("player_exp") { p, _ -> p.experience }.aliases("player_exp_total").build()
        buildVisitorSensitive("player_exp_to_next") { p, _ -> Player.calculateRequireExperience(p.experienceLevel + 1) }.aliases().build()
        buildVisitorSensitive("player_exp_level") { p, _ -> p.experienceLevel }.aliases().build()
        buildVisitorSensitive("player_speed") { p, _ -> p.movementSpeed }.aliases().build()
        buildVisitorSensitive("player_max_air") { p, _ -> p.getDataPropertyInt(Entity.DATA_MAX_AIR) }.updateInterval(100).build()
        buildVisitorSensitive("player_remaining_air") { p, _ -> p.getDataPropertyInt(Entity.DATA_AIR) }.updateInterval(10).build()
        buildVisitorSensitive("player_item_in_hand") { p, _ -> p.inventory?.itemInHand?.name }.updateInterval(10).build()

        val server = this.server
        val runtime = Runtime.getRuntime()

        buildStatic("server_online") { _ -> server.onlinePlayers.size }.aliases().build()
        buildStatic("server_max_players") { _ -> server.maxPlayers }.aliases().build()
        buildStatic("server_motd") { _ -> server.network.name }.aliases().build()
        buildStatic("server_ram_used") { _ -> (runtime.totalMemory() - runtime.freeMemory()).bytes2MB().round(configuration.coordsAccuracy) }.aliases().build()
        buildStatic("server_ram_free") { _ -> runtime.freeMemory().bytes2MB().round(configuration.coordsAccuracy) }.aliases().build()
        buildStatic("server_ram_total") { _ -> runtime.totalMemory().bytes2MB().round(configuration.coordsAccuracy) }.aliases().build()
        buildStatic("server_ram_max") { _ -> runtime.maxMemory().bytes2MB().round(configuration.coordsAccuracy) }.aliases().build()
        buildStatic("server_cores") { _ -> runtime.availableProcessors() }.aliases().build()
        buildStatic("server_tps") { _ -> server.ticksPerSecondAverage }.aliases().build()
        buildStatic("server_uptime") { _ -> (System.currentTimeMillis() - Nukkit.START_TIME).formatAsTime(configuration.timeFormat) }.aliases().build()

        buildStatic("time") { _ -> formatTime(System.currentTimeMillis()) }.updateInterval(10).build()
    }
}