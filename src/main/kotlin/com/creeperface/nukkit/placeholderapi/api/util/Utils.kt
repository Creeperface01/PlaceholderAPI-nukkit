package com.creeperface.nukkit.placeholderapi.api.util

import com.creeperface.nukkit.placeholderapi.util.trimSides
import java.util.regex.Pattern

/**
 * @author CreeperFace
 */

private val pattern = Pattern.compile("%(.+?)%")

fun String.matchPlaceholders(): List<MatchedGroup> {
    val matcher = pattern.matcher(this)
    val list = mutableListOf<MatchedGroup>()

    while (matcher.find()) {
        val s = matcher.group().trimSides(1, 1)

        list.add(MatchedGroup(s, matcher.start(), matcher.end()))
    }

    return list
}

data class MatchedGroup(val value: String, val start: Int, val end: Int)
