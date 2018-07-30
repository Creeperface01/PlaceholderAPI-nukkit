package com.creeperface.nukkit.placeholderapi.placeholder

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.plugin.Plugin
import com.creeperface.nukkit.placeholderapi.api.Placeholder
import com.creeperface.nukkit.placeholderapi.api.event.PlaceholderChangeListener
import com.creeperface.nukkit.placeholderapi.api.event.PlaceholderUpdateEvent
import java.util.*

/**
 * @author CreeperFace
 */
abstract class BasePlaceholder<T>(override val name: String, override val updateInterval: Int, override val autoUpdate: Boolean, override val aliases: Set<String>) : Placeholder<T> {

    private val changeListeners = mutableMapOf<Plugin, PlaceholderChangeListener<T>>()

    protected var value: T? = null
    var lastUpdate: Long = 0
    val server: Server = Server.getInstance()

    override fun getValue(player: Player?): String {
        val time = System.currentTimeMillis()

        if (value == null || readyToUpdate()) {
            value = loadValue()
            lastUpdate = time
        }

        return safeValue()
    }

    protected abstract fun loadValue(player: Player? = null): T?

    protected fun safeValue() = value?.toString() ?: name

    @JvmOverloads
    protected fun checkForUpdate(player: Player?, force: Boolean = false): Boolean {
        if (!force && !readyToUpdate())
            return false

        val time = System.currentTimeMillis()
        val newVal = loadValue(player)

        if (!Objects.equals(value, newVal)) {
            run {
                val ev = PlaceholderUpdateEvent(this, value, newVal)
                server.pluginManager.callEvent(ev)
            }

            changeListeners.forEach { _, listener -> listener.onChange(value, newVal) }

            value = newVal
            lastUpdate = time
            return true
        }

        return false
    }

    abstract override fun forceUpdate(player: Player?): String

    override fun autoUpdate() {
        if (changeListeners.isNotEmpty())
            checkForUpdate(null)
    }

    override fun addListener(plugin: Plugin, listener: PlaceholderChangeListener<T>) {
        changeListeners[plugin] = listener
    }

    override fun removeListener(plugin: Plugin) = changeListeners.remove(plugin)

    protected open fun readyToUpdate() = updateInterval >= 0 && (value == null || updateInterval == 0 || System.currentTimeMillis() - lastUpdate > intervalMillis())

    fun intervalMillis() = updateInterval * 50
}