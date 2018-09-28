package com.creeperface.nukkit.placeholderapi.api

import cn.nukkit.Player
import com.creeperface.nukkit.placeholderapi.PlaceholderAPIIml
import com.creeperface.nukkit.placeholderapi.api.util.MatchedGroup
import com.creeperface.nukkit.placeholderapi.api.util.matchPlaceholders
import java.util.function.Function
import java.util.function.Supplier

/**
 * @author CreeperFace
 */
interface PlaceholderAPI {

    companion object {
        fun getInstance(): com.creeperface.nukkit.placeholderapi.api.PlaceholderAPI {
            return PlaceholderAPIIml.instance
        }
    }

    fun <T> staticPlaceholder(name: String, loader: Supplier<T?>, vararg aliases: String)

//    fun <T> staticPlaceholder(name: String, loader: () -> T?, vararg aliases: String)

    fun <T> staticPlaceholder(name: String, loader: Supplier<T?>, updateInterval: Int) {
        staticPlaceholder(name, loader, updateInterval, false)
    }

//    fun <T> staticPlaceholder(name: String, loader: () -> T?, updateInterval: Int) {
//        staticPlaceholder(name, loader, updateInterval, false)
//    }

    fun <T> staticPlaceholder(name: String, loader: Supplier<T?>, updateInterval: Int = -1, autoUpdate: Boolean = false, vararg aliases: String)

//    fun <T> staticPlaceholder(name: String, loader: () -> T?, updateInterval: Int = -1, autoUpdate: Boolean = false, vararg aliases: String)

    fun <T> visitorSensitivePlaceholder(name: String, loader: Function<Player, T?>, vararg aliases: String)

//    fun <T> visitorSensitivePlaceholder(name: String, loader: (Player) -> T?, vararg aliases: String)

    fun <T> visitorSensitivePlaceholder(name: String, loader: Function<Player, T?>, updateInterval: Int) {
        visitorSensitivePlaceholder(name, loader, updateInterval, false)
    }

//    fun <T> visitorSensitivePlaceholder(name: String, loader: (Player) -> T?, updateInterval: Int) {
//        visitorSensitivePlaceholder(name, loader, updateInterval, false)
//    }

    fun <T> visitorSensitivePlaceholder(name: String, loader: Function<Player, T?>, updateInterval: Int = -1, autoUpdate: Boolean = false, vararg aliases: String)

//    fun <T> visitorSensitivePlaceholder(name: String, loader: (Player) -> T?, updateInterval: Int = -1, autoUpdate: Boolean = false, vararg aliases: String)

    fun registerPlaceholder(placeholder: Placeholder<out Any?>)

    fun getPlaceholder(key: String): Placeholder<out Any?>?

    fun getPlaceholders(): Map<String, Placeholder<out Any?>>

    fun getValue(key: String) = getValue(key, null)

    fun getValue(key: String, visitor: Player?) = getValue(key, visitor, key)

    fun getValue(key: String, visitor: Player? = null, defaultValue: String? = key): String?

    fun updatePlaceholder(key: String) = updatePlaceholder(key, null)

    fun updatePlaceholder(key: String, visitor: Player?)

    fun translateString(input: String) = translateString(input, null)

    fun translateString(input: String, visitor: Player? = null): String

    fun translateString(input: String, visitor: Player?, matched: Collection<MatchedGroup>): String

    fun findPlaceholders(input: String) = findPlaceholders(input.matchPlaceholders())

    fun findPlaceholders(matched: Collection<MatchedGroup>): List<Placeholder<out Any?>>
}