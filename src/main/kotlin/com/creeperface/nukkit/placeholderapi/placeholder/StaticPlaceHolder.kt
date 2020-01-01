package com.creeperface.nukkit.placeholderapi.placeholder

import cn.nukkit.Player
import com.creeperface.nukkit.placeholderapi.api.PlaceholderParameters
import com.creeperface.nukkit.placeholderapi.api.util.AnyContext
import com.creeperface.nukkit.placeholderapi.api.util.AnyScopeClass
import com.creeperface.nukkit.placeholderapi.api.util.PFormatter
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
        private val loader: (PlaceholderParameters, AnyContext) -> T?

) : BasePlaceholder<T>(
        name,
        updateInterval,
        autoUpdate,
        aliases,
        processParameters,
        scope,
        type,
        formatter
) {

    override fun loadValue(parameters: PlaceholderParameters, context: AnyContext, player: Player?): T? {
        return loader(parameters, context)
    }

    override fun forceUpdate(parameters: PlaceholderParameters, context: AnyContext, player: Player?): String {
        checkForUpdate(parameters)

        return safeValue()
    }
}