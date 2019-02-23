package com.creeperface.nukkit.placeholderapi.api.util

import com.creeperface.nukkit.placeholderapi.util.trimSides
import java.util.regex.Pattern

/**
 * @author CreeperFace
 */

private val pattern = Pattern.compile("%(.+(<(?<params>.+)>)?)%")

fun String.matchPlaceholders(): List<MatchedGroup> {
    val matcher = pattern.matcher(this)
    val list = mutableListOf<MatchedGroup>()

    while (matcher.find()) {
        val s = matcher.group().trimSides(1, 1)

        val params = mutableMapOf<String, String>()

        matcher.group("params")?.let { paramGroup ->
            paramGroup.split(';').forEach { param ->
                val paramEntry = param.split('=')

                if (paramEntry.size != 2) {
                    return@forEach
                }

                params[paramEntry[0]] = paramEntry[1]
            }
        }

        list.add(MatchedGroup(s, matcher.start(), matcher.end(), params))
    }

    return list
}

data class MatchedGroup(val value: String, val start: Int, val end: Int, val params: Map<String, String> = emptyMap())
