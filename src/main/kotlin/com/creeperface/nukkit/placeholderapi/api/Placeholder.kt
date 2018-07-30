package com.creeperface.nukkit.placeholderapi.api

import cn.nukkit.Player
import cn.nukkit.plugin.Plugin
import com.creeperface.nukkit.placeholderapi.api.event.PlaceholderChangeListener
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


/**
 * @author CreeperFace
 */
interface Placeholder<T> {

    /**
     * Placeholder name
     */
    val name: String

    /**
     * Placeholder aliases
     */
    val aliases: Set<String>

    /**
     * Update interval in ticks
     */
    val updateInterval: Int

    /**
     * If placeholder should be automatically updated
     */
    val autoUpdate: Boolean

    fun getValue() = getValue(null)

    fun getValue(player: Player? = null): String

    fun forceUpdate() = forceUpdate(null)

    fun forceUpdate(player: Player? = null): String

    fun addListener(plugin: Plugin, listener: PlaceholderChangeListener<T>)

    fun removeListener(plugin: Plugin): PlaceholderChangeListener<T>?

    fun autoUpdate()

    fun isVisitorSensitive() = false

    fun getType(): Type {
        val mySuperclass = this::class.java.genericSuperclass
        return (mySuperclass as ParameterizedType).actualTypeArguments[0]
    }
}