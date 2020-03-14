package com.creeperface.nukkit.placeholderapi.api.event

import cn.nukkit.event.Event
import cn.nukkit.event.HandlerList
import com.creeperface.nukkit.placeholderapi.api.PlaceholderAPI

class PlaceholderAPIInitializeEvent(
        val api: PlaceholderAPI
) : Event() {

    companion object {
        @JvmStatic
        private val handlers = HandlerList()

        @JvmStatic
        fun getHandlers(): HandlerList {
            return handlers
        }
    }
}