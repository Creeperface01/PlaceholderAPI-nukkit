package com.creeperface.nukkit.placeholderapi.placeholder.data

import cn.nukkit.Player
import com.creeperface.nukkit.placeholderapi.PlaceholderAPIIml
import com.creeperface.nukkit.placeholderapi.api.PlaceholderParameters
import com.creeperface.nukkit.placeholderapi.placeholder.StaticPlaceHolder
import com.creeperface.nukkit.placeholderapi.placeholder.VisitorSensitivePlaceholder
import com.google.common.base.Preconditions
import java.util.function.BiFunction
import java.util.function.Function

/**
 * @author CreeperFace
 */
class PlaceholderBuilder<T : Any>(private val name: String) {

    private var async = false //TODO: currently not implemented

    private var updateInterval = -1

    private var autoUpdate = false

//    private val parameters = mutableSetOf<PlaceholderParameter>()

    private var allowParameters = false

    private var staticLoader: (Function<PlaceholderParameters, T?>)? = null

    private var visitorLoader: (BiFunction<Player, PlaceholderParameters, T?>)? = null

    private val aliases = mutableSetOf<String>()

    fun async(value: Boolean) = apply { async = value }

    fun updateInterval(value: Int) = apply { updateInterval = value }

    fun autoUpdate(value: Boolean) = apply { autoUpdate = value }

    fun allowParameters(value: Boolean): PlaceholderBuilder<T> {
        this.allowParameters = value
        return this
    }

//    fun parameters(value: Collection<String>) = apply { parameters.addAll(value.map { PlaceholderParameter(it) }) }
//
//    fun parameters(vararg value: String) = apply { parameters.addAll(value.map { PlaceholderParameter(it) }) }

    fun aliases(value: Collection<String>) = apply { aliases.addAll(value) }

    fun aliases(vararg value: String) = apply { aliases.addAll(value) }

    fun loader(value: Function<PlaceholderParameters, T?>) = apply { staticLoader = value }

    fun loader(value: BiFunction<Player, PlaceholderParameters, T?>) = apply { visitorLoader = value }

    fun build(api: PlaceholderAPIIml) {
        Preconditions.checkArgument(staticLoader == null && visitorLoader == null, "Loader not specified")

        val placeholder = if (staticLoader == null) VisitorSensitivePlaceholder(name, updateInterval, autoUpdate, aliases, allowParameters, visitorLoader!!) else StaticPlaceHolder(name, updateInterval, autoUpdate, aliases, allowParameters, staticLoader!!)

        api.registerPlaceholder(placeholder)
    }
}