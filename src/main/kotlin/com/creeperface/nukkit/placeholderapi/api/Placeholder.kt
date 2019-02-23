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
     * Whether placeholder should be automatically updated
     */
    val autoUpdate: Boolean

    /**
     * Whether placeholder should take parameters when loading new value, s
     */
    val allowParameters: Boolean

    fun getValue() = getValue(null)

    fun getValue(parameters: Map<String, String> = emptyMap()) = getValue(parameters, null)

    fun getValue(player: Player? = null) = getValue(emptyMap(), player)

    fun getValue(parameters: Map<String, String> = emptyMap(), player: Player? = null): String

    fun getDirectValue() = getDirectValue(null)

    fun getDirectValue(player: Player? = null): T?

    fun forceUpdate() = forceUpdate(player = null)

    fun forceUpdate(parameters: Map<String, String> = emptyMap(), player: Player? = null): String

    fun addListener(plugin: Plugin, listener: PlaceholderChangeListener<T>)

    fun removeListener(plugin: Plugin): PlaceholderChangeListener<T>?

    fun autoUpdate()

    fun isVisitorSensitive() = false

    fun updateOrExecute(parameters: Map<String, String> = emptyMap(), player: Player? = null, action: Runnable)

    fun getType(): Type {
        val mySuperclass = this::class.java.genericSuperclass
        return (mySuperclass as ParameterizedType).actualTypeArguments[0]
    }
}