package com.creeperface.nukkit.placeholderapi.api

import com.creeperface.nukkit.placeholderapi.api.util.MatchedGroup

/**
 * @author CreeperFace
 */
data class PlaceholderParameters(private val params: Map<String, Parameter>, private val unnamed: List<Parameter>) {

    fun single() = unnamed.firstOrNull() ?: params.values.firstOrNull()

    operator fun get(key: String) = params[key]

    fun getNamed() = params.toMap()

    fun getUnnamed() = unnamed.toList()

    fun getAll() = params.values + unnamed

    data class Parameter(val name: String?, val value: String, val matchedGroup: MatchedGroup? = null)

    companion object {
        val EMPTY = PlaceholderParameters(emptyMap(), emptyList())
    }
}