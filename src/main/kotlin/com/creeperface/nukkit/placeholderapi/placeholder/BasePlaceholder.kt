package com.creeperface.nukkit.placeholderapi.placeholder

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.plugin.Plugin
import com.creeperface.nukkit.placeholderapi.api.Placeholder
import com.creeperface.nukkit.placeholderapi.api.PlaceholderParameters
import com.creeperface.nukkit.placeholderapi.api.event.PlaceholderChangeListener
import com.creeperface.nukkit.placeholderapi.api.event.PlaceholderUpdateEvent
import com.creeperface.nukkit.placeholderapi.util.toFormattedString
import java.util.*

/**
 * @author CreeperFace
 */
abstract class BasePlaceholder<T : Any?>(override val name: String, override val updateInterval: Int, override val autoUpdate: Boolean, override val aliases: Set<String>, override val allowParameters: Boolean) : Placeholder<T> {

    protected val changeListeners = mutableMapOf<Plugin, PlaceholderChangeListener<T>>()

    protected var value: T? = null
    var lastUpdate: Long = 0
    val server: Server = Server.getInstance()

    override fun getValue(parameters: PlaceholderParameters, player: Player?): String {
        if (value == null || readyToUpdate()) {
            checkForUpdate(player = player)
        }

        return safeValue()
    }

    override fun getDirectValue(parameters: PlaceholderParameters, player: Player?): T? {
        getValue(parameters, player)

        return value
    }

    override fun updateOrExecute(parameters: PlaceholderParameters, player: Player?, action: Runnable) {
        var updated = false

        if (value == null || readyToUpdate()) {
            updated = checkForUpdate(parameters, player)
        }

        if (!updated) {
            action.run()
        }
    }

    protected abstract fun loadValue(parameters: PlaceholderParameters, player: Player? = null): T?

    protected fun safeValue() = value?.toFormattedString() ?: name

    @JvmOverloads
    protected fun checkForUpdate(parameters: PlaceholderParameters = PlaceholderParameters.EMPTY, player: Player? = null, force: Boolean = false): Boolean {
        if (!force && !readyToUpdate())
            return false

        return checkValueUpdate(value, loadValue(parameters, player), player)
    }

    protected open fun checkValueUpdate(value: T?, newVal: T?, player: Player? = null): Boolean {
        if (!Objects.equals(value, newVal)) {
            Server.getInstance().scheduler.scheduleTask {
                run {
                    val ev = PlaceholderUpdateEvent(this, value, newVal, player)
                    server.pluginManager.callEvent(ev)
                }

                changeListeners.forEach { _, listener -> listener.onChange(value, newVal, player) }
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