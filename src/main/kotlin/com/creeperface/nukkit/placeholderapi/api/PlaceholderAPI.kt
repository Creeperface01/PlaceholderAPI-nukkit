package com.creeperface.nukkit.placeholderapi.api

import cn.nukkit.Player
import com.creeperface.nukkit.placeholderapi.PlaceholderAPIIml
import com.creeperface.nukkit.placeholderapi.api.util.MatchedGroup
import com.creeperface.nukkit.placeholderapi.api.util.matchPlaceholders
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
    fun <T> staticPlaceholder(name: String, loader: Supplier<T?>, vararg aliases: String) where T : Any? = staticPlaceholder(name, Function<Map<kotlin.String, kotlin.String>, T?> { loader.get() }, *aliases)

    @JvmDefault
    fun <T> staticPlaceholder(name: String, loader: Function<Map<String, String>, T?>, vararg aliases: String) where T : Any? = staticPlaceholder(name, loader, 20, false, *aliases)

    @Deprecated(
            message = "Replaced with a method containing loader with placeholder parameters",
            replaceWith = ReplaceWith(
                    expression = "staticPlaceholder(name, loader, updateInterval)"
            )
    )
    @JvmDefault
    fun <T> staticPlaceholder(name: String, loader: Supplier<T?>, updateInterval: Int) where T : Any? = staticPlaceholder(name, Function<Map<kotlin.String, kotlin.String>, T?> { loader.get() }, updateInterval)

    @JvmDefault
    fun <T> staticPlaceholder(name: String, loader: Function<Map<String, String>, T?>, updateInterval: Int) where T : Any? = staticPlaceholder(name, loader, updateInterval, false)


    @Deprecated(
            message = "Replaced with a method containing loader with placeholder parameters",
            replaceWith = ReplaceWith(
                    expression = "staticPlaceholder(name, loader, updateInterval, autoUpdate, aliases)"
            )
    )
    @JvmDefault
    fun <T> staticPlaceholder(name: String, loader: Supplier<T?>, updateInterval: Int = -1, autoUpdate: Boolean = false, vararg aliases: String) where T : Any? = staticPlaceholder(name, Function<Map<kotlin.String, kotlin.String>, T?> { loader.get() }, updateInterval, autoUpdate, *aliases)

    fun <T> staticPlaceholder(name: String, loader: Function<Map<String, String>, T?>, updateInterval: Int = -1, autoUpdate: Boolean = false, vararg aliases: String) where T : Any?

    @Deprecated(
            message = "Replaced with a method containing loader with placeholder parameters",
            replaceWith = ReplaceWith(
                    expression = "visitorSensitivePlaceholder(name, loader, aliases)"
            )
    )
    @JvmDefault
    fun <T> visitorSensitivePlaceholder(name: String, loader: Function<Player, T?>, vararg aliases: String) where T : Any? = visitorSensitivePlaceholder(name, BiFunction<Player, Map<String, String>, T?> { p, _ -> loader.apply(p) }, *aliases)

    @JvmDefault
    fun <T> visitorSensitivePlaceholder(name: String, loader: BiFunction<Player, Map<String, String>, T?>, vararg aliases: String) where T : Any? = visitorSensitivePlaceholder(name, loader, 20, false, *aliases)

    @Deprecated(
            message = "Replaced with a method containing loader with placeholder parameters",
            replaceWith = ReplaceWith(
                    expression = "visitorSensitivePlaceholder(name, loader, updateInterval)"
            )
    )
    @JvmDefault
    fun <T> visitorSensitivePlaceholder(name: String, loader: Function<Player, T?>, updateInterval: Int) where T : Any? = visitorSensitivePlaceholder(name, BiFunction<Player, Map<String, String>, T?> { p, _ -> loader.apply(p) }, updateInterval, false)

    fun <T> visitorSensitivePlaceholder(name: String, loader: BiFunction<Player, Map<String, String>, T?>, updateInterval: Int) where T : Any? = visitorSensitivePlaceholder(name, loader, updateInterval, false)

    @Deprecated(
            message = "Replaced with a method containing loader with placeholder parameters",
            replaceWith = ReplaceWith(
                    expression = "visitorSensitivePlaceholder(name, loader, updateInterval, autoUpdate, aliases)"
            )
    )
    @JvmDefault
    fun <T> visitorSensitivePlaceholder(name: String, loader: Function<Player, T?>, updateInterval: Int = -1, autoUpdate: Boolean = false, vararg aliases: String) where T : Any? = visitorSensitivePlaceholder(name, BiFunction<Player, Map<String, String>, T?> { p, _ -> loader.apply(p) }, updateInterval, autoUpdate, *aliases)

    fun <T> visitorSensitivePlaceholder(name: String, loader: BiFunction<Player, Map<String, String>, T?>, updateInterval: Int = -1, autoUpdate: Boolean = false, vararg aliases: String) where T : Any?

    fun registerPlaceholder(placeholder: Placeholder<out Any?>)

    fun getPlaceholder(key: String): Placeholder<out Any?>?

    fun getPlaceholders(): Map<String, Placeholder<out Any?>>

    @JvmDefault
    fun getValue(key: String) = getValue(key, null)

    @JvmDefault
    fun getValue(key: String, visitor: Player?) = getValue(key, visitor, key)

    @JvmDefault
    fun getValue(key: String, visitor: Player? = null, defaultValue: String? = key) = getValue(key, visitor, defaultValue, emptyMap())

    fun getValue(key: String, visitor: Player? = null, defaultValue: String? = key, params: Map<String, String> = emptyMap()): String?

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
}