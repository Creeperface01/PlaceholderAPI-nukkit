package com.creeperface.nukkit.placeholderapi.placeholder

import cn.nukkit.Player
import com.creeperface.nukkit.placeholderapi.api.PlaceholderParameters
import com.creeperface.nukkit.placeholderapi.api.scope.Scope
import java.util.function.Function

/**
 * @author CreeperFace
 */
open class StaticPlaceHolder<T : Any?>(
        name: String,
        updateInterval: Int,
        autoUpdate: Boolean,
        aliases: Set<String>,
        processParameters: Boolean,
        scope: Scope,
        private val loader: Function<PlaceholderParameters, T?>

) : BasePlaceholder<T>(
        name,
        updateInterval,
        autoUpdate,
        aliases,
        processParameters,
        scope
) {

    override fun loadValue(parameters: PlaceholderParameters, player: Player?): T? {
        return loader.apply(parameters)
    }

    override fun forceUpdate(parameters: PlaceholderParameters, player: Player?): String {
        checkForUpdate(parameters)

        return safeValue()
    }
}