package com.creeperface.nukkit.placeholderapi.placeholder

import cn.nukkit.Player
import com.creeperface.nukkit.placeholderapi.api.PlaceholderParameters
import com.creeperface.nukkit.placeholderapi.api.util.*
import kotlin.reflect.KClass

/**
 * @author CreeperFace
 */
open class StaticPlaceHolder<T : Any>(
        name: String,
        updateInterval: Int,
        autoUpdate: Boolean,
        aliases: Set<String>,
        processParameters: Boolean,
        scope: AnyScopeClass,
        type: KClass<T>,
        formatter: PFormatter,
        loader: Loader<T>
) : BasePlaceholder<T>(
        name,
        updateInterval,
        autoUpdate,
        aliases,
        processParameters,
        scope,
        type,
        formatter,
        loader
) {

    override fun loadValue(parameters: PlaceholderParameters, context: AnyContext, player: Player?): T? {
        return loader(ValueEntry(null, parameters, context))
    }

    override fun forceUpdate(parameters: PlaceholderParameters, context: AnyContext, player: Player?): String {
        checkForUpdate(parameters)

        return safeValue()
    }
}