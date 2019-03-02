package com.creeperface.nukkit.placeholderapi.api.event

import cn.nukkit.Player
import cn.nukkit.event.Event
import cn.nukkit.event.HandlerList
import com.creeperface.nukkit.placeholderapi.placeholder.BasePlaceholder

/**
 * @author CreeperFace
 */
class PlaceholderUpdateEvent<T : Any?>(val placeholder: BasePlaceholder<T>, val oldValue: Any?, val newValue: Any?, val player: Player?) : Event() {

    companion object {
        @JvmStatic
        private val handlers = HandlerList()

        @JvmStatic
        fun getHandlers(): HandlerList {
            return handlers
        }
    }
}