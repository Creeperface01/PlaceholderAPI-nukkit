package com.creeperface.nukkit.placeholderapi.api.event

import cn.nukkit.Player

/**
 * @author CreeperFace
 */
interface PlaceholderChangeListener<T> {

    fun onChange(oldVal: T?, newVal: T?, player: Player?)
}