package com.creeperface.nukkit.placeholderapi.placeholder

import cn.nukkit.Player
import com.google.common.cache.CacheBuilder
import java.util.concurrent.TimeUnit

/**
 * @author CreeperFace
 */
open class VisitorSensitivePlaceholder<T>(name: String, updateInterval: Int, autoUpdate: Boolean, aliases: Set<String>, private val loader: (Player) -> T?) : BasePlaceholder<T>(name, updateInterval, autoUpdate, aliases) {

    private val cache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(if (updateInterval > 0) (updateInterval * 50).toLong() else 30000, TimeUnit.MILLISECONDS)
            .build<Player, Entry>()

    override fun getValue(player: Player?): String {
        if (player == null)
            return name

        val cached = cache.getIfPresent(player)

        cached?.let {
            if (updateInterval > 0 && System.currentTimeMillis() - cached.lastUpdate < intervalMillis()) {
                return cached.value
            }
        }

        value = null

        if (checkForUpdate(player)) {
            if (value != null) {
                cache.put(player, Entry(value.toString()))
            }
        }

        return safeValue()
    }

    override fun loadValue(player: Player?) = if (player != null) loader.invoke(player) else null

    override fun forceUpdate(player: Player?): String {
        if (player == null)
            return name

        if (checkForUpdate(player, true)) {
            if (value != null) {
                cache.put(player, Entry(value.toString()))
            }
        }

        return safeValue()
    }

    override fun isVisitorSensitive() = true

    override fun readyToUpdate() = true

    private data class Entry(val value: String, val lastUpdate: Long = System.currentTimeMillis())
}