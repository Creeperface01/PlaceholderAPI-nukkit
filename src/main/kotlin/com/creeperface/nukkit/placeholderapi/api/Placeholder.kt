package com.creeperface.nukkit.placeholderapi.api

import cn.nukkit.Player
import cn.nukkit.plugin.Plugin
import com.creeperface.nukkit.placeholderapi.api.event.PlaceholderChangeListener
import com.creeperface.nukkit.placeholderapi.api.util.AnyContext
import com.creeperface.nukkit.placeholderapi.api.util.AnyScope
import com.creeperface.nukkit.placeholderapi.api.util.PFormatter
import kotlin.reflect.KClass

/**
 * @author CreeperFace
 */
interface Placeholder<T : Any> {

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
    val processParameters: Boolean

    /**
     * A scope where this placeholder can be applied
     */
    val scope: AnyScope

    val returnType: KClass<T>

    /**
     * A Formatter instance for properly formatted output
     */
    val formatter: PFormatter

    @JvmDefault
    fun getValue() = getValue(PlaceholderParameters.EMPTY, scope.defaultContext, null)

    @JvmDefault
    fun getValue(player: Player? = null) = getValue(PlaceholderParameters.EMPTY, scope.defaultContext, player)

    @JvmDefault
    fun getValue(parameters: PlaceholderParameters = PlaceholderParameters.EMPTY, context: AnyContext = scope.defaultContext, player: Player? = null): String

    @JvmDefault
    fun getDirectValue(player: Player? = null) = getDirectValue(PlaceholderParameters.EMPTY, player)

    @JvmDefault
    fun getDirectValue(parameters: PlaceholderParameters = PlaceholderParameters.EMPTY, player: Player? = null) = getDirectValue(parameters, scope.defaultContext, player)

    @JvmDefault
    fun getDirectValue(parameters: PlaceholderParameters = PlaceholderParameters.EMPTY, context: AnyContext = scope.defaultContext, player: Player? = null): T?

    @JvmDefault
    fun forceUpdate(parameters: PlaceholderParameters = PlaceholderParameters.EMPTY, context: AnyContext = scope.defaultContext, player: Player? = null): String

    fun addListener(plugin: Plugin, listener: PlaceholderChangeListener<T>)

    fun removeListener(plugin: Plugin): PlaceholderChangeListener<T>?

    fun autoUpdate()

    fun isVisitorSensitive() = false

    fun updateOrExecute(parameters: PlaceholderParameters = PlaceholderParameters.EMPTY, context: AnyContext = scope.defaultContext, player: Player? = null, action: Runnable)
}