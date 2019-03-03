package com.creeperface.nukkit.placeholderapi.util

import com.creeperface.nukkit.placeholderapi.api.Placeholder

/**
 * @author CreeperFace
 */
class PlaceholderGroup(val prefix: String, val placeholders: Map<String, Placeholder<Any>>) {

    private val groups = mutableMapOf<String, PlaceholderGroup>()

    fun hasSubGroups() = groups.isNotEmpty()

    fun addGroup(group: PlaceholderGroup) {
        groups[group.prefix] = group
    }
}