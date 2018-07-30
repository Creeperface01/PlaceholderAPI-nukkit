package com.creeperface.nukkit.placeholderapi.api.event

import com.creeperface.nukkit.placeholderapi.placeholder.data.PlaceholderParameter

/**
 * @author CreeperFace
 */
interface PlaceholderChangeListener<T> {

    fun onChange(oldVal: T?, newVal: T?, params: Set<PlaceholderParameter> = emptySet())
}