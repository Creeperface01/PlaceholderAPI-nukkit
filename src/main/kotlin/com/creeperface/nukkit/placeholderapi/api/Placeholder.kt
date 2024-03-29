package com.creeperface.nukkit.placeholderapi.api

import cn.nukkit.Player
import cn.nukkit.plugin.Plugin
import com.creeperface.nukkit.placeholderapi.api.event.PlaceholderChangeListener
import com.creeperface.nukkit.placeholderapi.api.scope.Scope
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

    fun getValue() = getValue(PlaceholderParameters.EMPTY, scope.defaultContext, null)

    fun getValue(player: Player? = null) = getValue(PlaceholderParameters.EMPTY, scope.defaultContext, player)

    fun getValue(parameters: PlaceholderParameters = PlaceholderParameters.EMPTY, context: AnyContext = scope.defaultContext, player: Player? = null): String

    fun getDirectValue(player: Player? = null) = getDirectValue(PlaceholderParameters.EMPTY, player)

    fun getDirectValue(parameters: PlaceholderParameters = PlaceholderParameters.EMPTY, player: Player? = null) = getDirectValue(parameters, scope.defaultContext, player)

    fun getDirectValue(parameters: PlaceholderParameters = PlaceholderParameters.EMPTY, context: AnyContext = scope.defaultContext, player: Player? = null): T?

    fun forceUpdate(parameters: PlaceholderParameters = PlaceholderParameters.EMPTY, context: AnyContext = scope.defaultContext, player: Player? = null): String

    fun addListener(plugin: Plugin, listener: PlaceholderChangeListener<T>)

    fun removeListener(plugin: Plugin): PlaceholderChangeListener<T>?

    fun autoUpdate()

    fun isVisitorSensitive() = false

    fun updateOrExecute(parameters: PlaceholderParameters = PlaceholderParameters.EMPTY, context: AnyContext = scope.defaultContext, player: Player? = null, action: Runnable)

    @Suppress("UNCHECKED_CAST")
    class VisitorEntry<T, ST, S : Scope<ST, S>>(
            override val player: Player,
            parameters: PlaceholderParameters,
            context: AnyContext
    ) : Entry<T, ST, S>(player, parameters, context as Scope<ST, S>.Context)

    open class Entry<T, ST, S : Scope<ST, S>>(
            open val player: Player?,
            val parameters: PlaceholderParameters,
            val context: Scope<ST, S>.Context
    ) {

        val contextVal = context.context

        @Suppress("UNCHECKED_CAST")
        fun <ST, S : Scope<ST, S>> scoped(clazz: KClass<S>, loader: (Scope<ST, S>.Context) -> T): T? {
            return loader(context as Scope<ST, S>.Context)
        }
    }
}