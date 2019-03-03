package com.creeperface.nukkit.placeholderapi.api

/**
 * @author CreeperFace
 */
class PlaceholderParameters(private val params: Map<String, String>, private val unnamed: List<String>) {

    fun single() = unnamed.firstOrNull() ?: params.values.firstOrNull()

    operator fun get(key: String) = params[key]

    fun getAll() = params.toMap()

    fun getUnnamed() = unnamed.toList()

    companion object {
        val EMPTY = PlaceholderParameters(emptyMap(), emptyList())
    }
}