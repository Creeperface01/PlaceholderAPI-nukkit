package com.creeperface.nukkit.placeholderapi.api.util

import java.util.regex.Pattern

/**
 * @author CreeperFace
 */

private val pattern = Pattern.compile("%((?<ph>\\w+)(<(?<params>.+)>)?)%")

fun String.matchPlaceholders(): List<MatchedGroup> {
    val matcher = pattern.matcher(this)
    val list = mutableListOf<MatchedGroup>()

    while (matcher.find()) {
        val s = matcher.group("ph")

        val params = mutableMapOf<String, String>()

        matcher.group("params")?.let { paramGroup ->
            paramGroup.split(';').forEach { param ->
                val paramEntry = param.split('=')

                if (paramEntry.size != 2) {
//                    MainLogger.getLogger().warning("Invalid parameter or wrong format supplied for placeholder '$s' ($param)")
                    return@forEach
                }

                if (paramEntry[0].isEmpty() || paramEntry[1].isEmpty()) {
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
