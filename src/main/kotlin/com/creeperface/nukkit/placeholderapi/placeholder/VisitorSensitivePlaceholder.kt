package com.creeperface.nukkit.placeholderapi.placeholder

import cn.nukkit.Player
import cn.nukkit.Server
import com.creeperface.nukkit.placeholderapi.api.PlaceholderParameters
import com.creeperface.nukkit.placeholderapi.api.event.PlaceholderUpdateEvent
import java.util.*
import java.util.function.BiFunction

/**
 * @author CreeperFace
 */
open class VisitorSensitivePlaceholder<T : Any?>(name: String, updateInterval: Int, autoUpdate: Boolean, aliases: Set<String>, allowParameters: Boolean, private val loader: BiFunction<Player, PlaceholderParameters, T?>) : BasePlaceholder<T>(name, updateInterval, autoUpdate, aliases, allowParameters) {

    private val cache = WeakHashMap<Player, Entry<T>>()

    override fun getValue(parameters: PlaceholderParameters, player: Player?): String {
        if (player == null)
            return name

        val cached = cache[player]

        cached?.let {
            if (updateInterval > 0 && System.currentTimeMillis() - cached.lastUpdate < intervalMillis()) {
                return cached.value.toString()
            } else {
                cache.remove(player)
            }
        }

        value = null

        if (checkForUpdate(parameters, player, true)) {
            if (value != null) {
                cache[player] = Entry(value)
            }
        }

        return safeValue()
    }

    override fun updateOrExecute(parameters: PlaceholderParameters, player: Player?, action: Runnable) {
        var updated = false

        val cached = cache[player]

        var needUpdate = true

        cached?.let {
            if (updateInterval > 0 && System.currentTimeMillis() - cached.lastUpdate < intervalMillis()) {
                needUpdate = false
            }
        }

        if (needUpdate) {
            value = null

            if (checkForUpdate(parameters, player)) {
                if (value != null) {
                    cache[player] = Entry(value)
                }

                updated = true
            }
        }

        if (!updated) {
            action.run()
        }
    }

    override fun loadValue(parameters: PlaceholderParameters, player: Player?) = if (player != null) loader.apply(player, parameters) else null

    override fun forceUpdate(parameters: PlaceholderParameters, player: Player?): String {
        if (player == null)
            return name

        if (checkForUpdate(parameters, player, true)) {
            if (value != null) {
                cache[player] = Entry(value)
            }
        }

        return safeValue()
    }

    override fun checkValueUpdate(value: T?, newVal: T?, player: Player?): Boolean {
        if (player == null)
            return false

        val value = cache[player]?.value ?: value

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

    override fun isVisitorSensitive() = true

    override fun readyToUpdate() = true

    data class Entry<T>(val value: T?, val lastUpdate: Long = System.currentTimeMillis())
}