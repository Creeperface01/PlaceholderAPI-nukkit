package com.creeperface.nukkit.placeholderapi.placeholder

import cn.nukkit.Player

/**
 * @author CreeperFace
 */
open class StaticPlaceHolder<T>(name: String, updateInterval: Int, autoUpdate: Boolean, aliases: Set<String>, private val loader: () -> T?) : BasePlaceholder<T>(name, updateInterval, autoUpdate, aliases) {

    override fun loadValue(player: Player?): T? {
        return loader()
    }

    override fun forceUpdate(player: Player?): String {
        checkForUpdate(player)

        return safeValue()
    }
}