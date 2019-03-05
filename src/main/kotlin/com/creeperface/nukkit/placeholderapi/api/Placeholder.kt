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
     * Whether placeholder should take parameters when loading new value
     *
     * @note set this to false if you don't handle parameters for better performance
     */
    val allowParameters: Boolean

    @JvmDefault
    fun getValue() = getValue(PlaceholderParameters.EMPTY, null)

    @JvmDefault
    fun getValue(player: Player? = null) = getValue(PlaceholderParameters.EMPTY, player)

    @JvmDefault
    fun getValue(parameters: PlaceholderParameters = PlaceholderParameters.EMPTY, player: Player? = null): String

    @JvmDefault
    fun getDirectValue(player: Player? = null) = getDirectValue(PlaceholderParameters.EMPTY, player)

    @JvmDefault
    fun getDirectValue(parameters: PlaceholderParameters = PlaceholderParameters.EMPTY, player: Player? = null): T?

    @JvmDefault
    fun forceUpdate(parameters: PlaceholderParameters = PlaceholderParameters.EMPTY, player: Player? = null): String

    fun addListener(plugin: Plugin, listener: PlaceholderChangeListener<T>)

    fun removeListener(plugin: Plugin): PlaceholderChangeListener<T>?

    fun autoUpdate()

    fun isVisitorSensitive() = false

    fun updateOrExecute(parameters: PlaceholderParameters = PlaceholderParameters.EMPTY, player: Player? = null, action: Runnable)

    fun getType(): Type {
        val mySuperclass = this::class.java.genericSuperclass
        return (mySuperclass as ParameterizedType).actualTypeArguments[0]
    }
}