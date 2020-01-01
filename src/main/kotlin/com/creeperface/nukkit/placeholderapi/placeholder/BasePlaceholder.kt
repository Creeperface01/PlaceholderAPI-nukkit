package com.creeperface.nukkit.placeholderapi.placeholder

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.plugin.Plugin
import com.creeperface.nukkit.placeholderapi.PlaceholderAPIIml
import com.creeperface.nukkit.placeholderapi.api.Placeholder
import com.creeperface.nukkit.placeholderapi.api.PlaceholderParameters
import com.creeperface.nukkit.placeholderapi.api.event.PlaceholderChangeListener
import com.creeperface.nukkit.placeholderapi.api.event.PlaceholderUpdateEvent
import com.creeperface.nukkit.placeholderapi.api.util.AnyContext
import com.creeperface.nukkit.placeholderapi.api.util.AnyScope
import com.creeperface.nukkit.placeholderapi.api.util.AnyScopeClass
import com.creeperface.nukkit.placeholderapi.api.util.PFormatter
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.staticProperties
import kotlin.reflect.jvm.isAccessible

/**
 * @author CreeperFace
 */
abstract class BasePlaceholder<T : Any>(override val name: String, override val updateInterval: Int, override val autoUpdate: Boolean, override val aliases: Set<String>, override val processParameters: Boolean, scope: AnyScopeClass, override val returnType: KClass<T>, override val formatter: PFormatter) : Placeholder<T> {

    protected val changeListeners = mutableMapOf<Plugin, PlaceholderChangeListener<T>>()

    protected var value: T? = null
    var lastUpdate: Long = 0
    val server: Server = Server.getInstance()

    override lateinit var scope: AnyScope

    init {
        run {
            scope.objectInstance?.let {
                this.scope = it
                return@run
            }

            val property = scope.staticProperties.find {
                if (!it.name.equals("instance", true)) {
                    return@find false
                }

                val classifier = it.returnType.classifier
                return@find classifier is KClass<*> && classifier.isSubclassOf(scope)
            } ?: throw RuntimeException("Could not find scope instance for class ${scope.qualifiedName}")

            property.isAccessible = true
            this.scope = property.get() as AnyScope
        }
    }

    override fun getValue(parameters: PlaceholderParameters, context: AnyContext, player: Player?): String {
        if (value == null || readyToUpdate()) {
            checkForUpdate(parameters, player = player, context = context)
        }

        return safeValue()
    }

    override fun getDirectValue(parameters: PlaceholderParameters, context: AnyContext, player: Player?): T? {
        getValue(parameters, context, player)

        return value
    }

    override fun updateOrExecute(parameters: PlaceholderParameters, context: AnyContext, player: Player?, action: Runnable) {
        var updated = false

        if (value == null || readyToUpdate()) {
            updated = checkForUpdate(parameters, context, player)
        }

        if (!updated) {
            action.run()
        }
    }

    protected abstract fun loadValue(parameters: PlaceholderParameters, context: AnyContext, player: Player? = null): T?

    protected fun safeValue() = value?.let { formatter(it) } ?: name

    @JvmOverloads
    protected fun checkForUpdate(parameters: PlaceholderParameters = PlaceholderParameters.EMPTY, context: AnyContext = scope.defaultContext, player: Player? = null, force: Boolean = false): Boolean {
        if (!force && !readyToUpdate())
            return false

        return checkValueUpdate(value, loadValue(parameters, context, player), player)
    }

    protected open fun checkValueUpdate(value: T?, newVal: T?, player: Player? = null): Boolean {
        if (!Objects.equals(value, newVal)) {
            Server.getInstance().scheduler.scheduleTask(PlaceholderAPIIml.instance) {
                run {
                    val ev = PlaceholderUpdateEvent(this, value, newVal, player)
                    server.pluginManager.callEvent(ev)
                }

                changeListeners.forEach { (_, listener) -> listener.onChange(value, newVal, player) }
            }

            this.value = newVal
            lastUpdate = System.currentTimeMillis()
            return true
        }

        return false
    }

    override fun autoUpdate() {
        if (changeListeners.isNotEmpty())
            checkForUpdate()
    }

    override fun addListener(plugin: Plugin, listener: PlaceholderChangeListener<T>) {
        changeListeners[plugin] = listener
    }

    override fun removeListener(plugin: Plugin) = changeListeners.remove(plugin)

    protected open fun readyToUpdate() = updateInterval >= 0 && (value == null || updateInterval == 0 || System.currentTimeMillis() - lastUpdate > intervalMillis())

    fun intervalMillis() = updateInterval * 50
}