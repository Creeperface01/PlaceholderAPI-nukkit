package com.creeperface.nukkit.placeholderapi.placeholder

import cn.nukkit.Player
import java.util.function.Function

/**
 * @author CreeperFace
 */
open class StaticPlaceHolder<T>(name: String, updateInterval: Int, autoUpdate: Boolean, aliases: Set<String>, allowParameters: Boolean, private val loader: Function<Map<String, String>, T?>) : BasePlaceholder<T>(name, updateInterval, autoUpdate, aliases, allowParameters) {

    override fun loadValue(parameters: Map<String, String>, player: Player?): T? {
        return loader.apply(parameters)
    }

    override fun forceUpdate(parameters: Map<String, String>, player: Player?): String {
        checkForUpdate(parameters)

        return safeValue()
    }
}