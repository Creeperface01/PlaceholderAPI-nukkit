package com.creeperface.nukkit.placeholderapi.api

import cn.nukkit.Player
import com.creeperface.nukkit.placeholderapi.PlaceholderAPIIml
import com.creeperface.nukkit.placeholderapi.api.util.MatchedGroup
import com.creeperface.nukkit.placeholderapi.api.util.matchPlaceholders
import java.util.*
import java.util.function.BiFunction
import java.util.function.Function
import java.util.function.Supplier

/**
 * @author CreeperFace
 */
interface PlaceholderAPI {

    companion object {

        @JvmStatic
        fun getInstance(): PlaceholderAPI {
            return PlaceholderAPIIml.instance
        }
    }

    @Deprecated(
            message = "Replaced with a method containing loader with placeholder parameters",
            replaceWith = ReplaceWith(
                    expression = "staticPlaceholder(name, loader, aliases)"
            )
    )
    @JvmDefault
    fun <T> staticPlaceholder(name: String, loader: Supplier<T?>, vararg aliases: String) where T : Any? = staticPlaceholder(name, Function<PlaceholderParameters, T?> { loader.get() }, *aliases)

    @JvmDefault
    fun <T> staticPlaceholder(name: String, loader: Function<PlaceholderParameters, T?>, vararg aliases: String) where T : Any? = staticPlaceholder(name, loader, 20, false, *aliases)

    @Deprecated(
            message = "Replaced with a method containing loader with placeholder parameters",
            replaceWith = ReplaceWith(
                    expression = "staticPlaceholder(name, loader, updateInterval)"
            )
    )
    @JvmDefault
    fun <T> staticPlaceholder(name: String, loader: Supplier<T?>, updateInterval: Int) where T : Any? = staticPlaceholder(name, Function<PlaceholderParameters, T?> { loader.get() }, updateInterval)

    @JvmDefault
    fun <T> staticPlaceholder(name: String, loader: Function<PlaceholderParameters, T?>, updateInterval: Int) where T : Any? = staticPlaceholder(name, loader, updateInterval, false)


    @Deprecated(
            message = "Replaced with a method containing loader with placeholder parameters",
            replaceWith = ReplaceWith(
                    expression = "staticPlaceholder(name, loader, updateInterval, autoUpdate, aliases)"
            )
    )
    @JvmDefault
    fun <T> staticPlaceholder(name: String, loader: Supplier<T?>, updateInterval: Int = -1, autoUpdate: Boolean = false, vararg aliases: String) where T : Any? = staticPlaceholder(name, Function<PlaceholderParameters, T?> { loader.get() }, updateInterval, autoUpdate, *aliases)

    fun <T> staticPlaceholder(name: String, loader: Function<PlaceholderParameters, T?>, updateInterval: Int = -1, autoUpdate: Boolean = false, vararg aliases: String) where T : Any?

    @Deprecated(
            message = "Replaced with a method containing loader with placeholder parameters",
            replaceWith = ReplaceWith(
                    expression = "visitorSensitivePlaceholder(name, loader, aliases)"
            )
    )
    @JvmDefault
    fun <T> visitorSensitivePlaceholder(name: String, loader: Function<Player, T?>, vararg aliases: String) where T : Any? = visitorSensitivePlaceholder(name, BiFunction<Player, PlaceholderParameters, T?> { p, _ -> loader.apply(p) }, *aliases)

    @JvmDefault
    fun <T> visitorSensitivePlaceholder(name: String, loader: BiFunction<Player, PlaceholderParameters, T?>, vararg aliases: String) where T : Any? = visitorSensitivePlaceholder(name, loader, 20, false, *aliases)

    @Deprecated(
            message = "Replaced with a method containing loader with placeholder parameters",
            replaceWith = ReplaceWith(
                    expression = "visitorSensitivePlaceholder(name, loader, updateInterval)"
            )
    )
    @JvmDefault
    fun <T> visitorSensitivePlaceholder(name: String, loader: Function<Player, T?>, updateInterval: Int) where T : Any? = visitorSensitivePlaceholder(name, BiFunction<Player, PlaceholderParameters, T?> { p, _ -> loader.apply(p) }, updateInterval, false)

    fun <T> visitorSensitivePlaceholder(name: String, loader: BiFunction<Player, PlaceholderParameters, T?>, updateInterval: Int) where T : Any? = visitorSensitivePlaceholder(name, loader, updateInterval, false)

    @Deprecated(
            message = "Replaced with a method containing loader with placeholder parameters",
            replaceWith = ReplaceWith(
                    expression = "visitorSensitivePlaceholder(name, loader, updateInterval, autoUpdate, aliases)"
            )
    )
    @JvmDefault
    fun <T> visitorSensitivePlaceholder(name: String, loader: Function<Player, T?>, updateInterval: Int = -1, autoUpdate: Boolean = false, vararg aliases: String) where T : Any? = visitorSensitivePlaceholder(name, BiFunction<Player, PlaceholderParameters, T?> { p, _ -> loader.apply(p) }, updateInterval, autoUpdate, *aliases)

    fun <T> visitorSensitivePlaceholder(name: String, loader: BiFunction<Player, PlaceholderParameters, T?>, updateInterval: Int = -1, autoUpdate: Boolean = false, vararg aliases: String) where T : Any?

    fun registerPlaceholder(placeholder: Placeholder<out Any?>)

    fun getPlaceholder(key: String): Placeholder<out Any?>?

    fun getPlaceholders(): Map<String, Placeholder<out Any?>>

    @JvmDefault
    fun getValue(key: String) = getValue(key, null)

    @JvmDefault
    fun getValue(key: String, visitor: Player?) = getValue(key, visitor, key)

    @JvmDefault
    fun getValue(key: String, visitor: Player? = null, defaultValue: String? = key) = getValue(key, visitor, defaultValue, PlaceholderParameters.EMPTY)

    fun getValue(key: String, visitor: Player? = null, defaultValue: String? = key, params: PlaceholderParameters = PlaceholderParameters.EMPTY): String?

    @JvmDefault
    fun updatePlaceholder(key: String) = updatePlaceholder(key, null)

    fun updatePlaceholder(key: String, visitor: Player?)

    @JvmDefault
    fun translateString(input: String) = translateString(input, null)

    @JvmDefault
    fun translateString(input: String, visitor: Player? = null) = translateString(input, visitor, input.matchPlaceholders())

    fun translateString(input: String, visitor: Player?, matched: Collection<MatchedGroup>): String

    fun findPlaceholders(input: String) = findPlaceholders(input.matchPlaceholders())

    fun findPlaceholders(matched: Collection<MatchedGroup>): List<Placeholder<out Any?>>

    fun formatTime(millis: Long): String

    fun formatDate(date: Date) = formatDate(date.time)

    fun formatDate(millis: Long): String

    fun formatBoolean(value: Boolean): String
}