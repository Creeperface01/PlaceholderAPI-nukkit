package com.creeperface.nukkit.placeholderapi.placeholder.data

import cn.nukkit.Player
import com.creeperface.nukkit.placeholderapi.PlaceholderAPIIml
import com.creeperface.nukkit.placeholderapi.placeholder.StaticPlaceHolder
import com.creeperface.nukkit.placeholderapi.placeholder.VisitorSensitivePlaceholder
import com.google.common.base.Preconditions

/**
 * @author CreeperFace
 */
class PlaceholderBuilder<T>(private val name: String) {

    private var async = false

    private var updateInterval = -1

    private var autoUpdate = false

    private val parameters = mutableSetOf<PlaceholderParameter>()

    private var staticLoader: (() -> T?)? = null

    private var visitorLoader: ((Player) -> T?)? = null

    private val aliases = mutableSetOf<String>()

    fun async(value: Boolean) = apply { async = value }

    fun updateInterval(value: Int) = apply { updateInterval = value }

    fun autoUpdate(value: Boolean) = apply { autoUpdate = value }

    fun parameters(value: Collection<String>) = apply { parameters.addAll(value.map { PlaceholderParameter(it) }) }

    fun parameters(vararg value: String) = apply { parameters.addAll(value.map { PlaceholderParameter(it) }) }

    fun aliases(value: Collection<String>) = apply { aliases.addAll(value) }

    fun aliases(vararg value: String) = apply { aliases.addAll(value) }

    fun loader(value: () -> T?) = apply { staticLoader = value }

    fun laoder(value: (Player) -> T?) = apply { visitorLoader = value }

    fun build(api: PlaceholderAPIIml) {
        Preconditions.checkArgument(staticLoader == null && visitorLoader == null, "Loader not specified")

        val placeholder = if (staticLoader == null) VisitorSensitivePlaceholder(name, updateInterval, autoUpdate, aliases, visitorLoader!!) else StaticPlaceHolder(name, updateInterval, autoUpdate, aliases, staticLoader!!)

        api.registerPlaceholder(placeholder)
    }
}