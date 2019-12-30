package com.creeperface.nukkit.placeholderapi.api.util

import com.creeperface.nukkit.placeholderapi.api.Placeholder
import com.creeperface.nukkit.placeholderapi.api.PlaceholderParameters
import com.creeperface.nukkit.placeholderapi.api.scope.Scope
import com.creeperface.nukkit.placeholderapi.util.Parser
import java.util.regex.Pattern

/**
 * @author CreeperFace
 */

typealias AnyScope = Scope<out Any?, *>

typealias AnyContext = Scope<out Any?, *>.Context
typealias AnyPlaceholder = Placeholder<out Any?>

typealias PlaceholderGroup = MutableMap<String, AnyPlaceholder>

private val pattern = Pattern.compile("%((?<ph>\\w+)(<(?<params>.+)>)?)%")

fun String.matchPlaceholders() = Parser.parse(this)

//fun String.matchPlaceholders(): List<MatchedGroup> {
//    val matcher = pattern.matcher(this)
//    val list = mutableListOf<MatchedGroup>()
//
//    while (matcher.find()) {
//        val s = matcher.group("ph")
//
//        val params = mutableMapOf<String, PlaceholderParameters.Parameter>()
//        val unnamedParams = mutableListOf<PlaceholderParameters.Parameter>()
//
//        matcher.group("params")?.let { paramGroup ->
//            val paramList = paramGroup.split(';')
//
//            paramList.forEach { param ->
//                val paramEntry = param.split('=')
//
//                if (paramEntry.size != 2) {
//                    if (paramEntry.size == 1) {
//                        unnamedParams.add(PlaceholderParameters.Parameter(null, paramEntry[0].replace('&', '§')))
//                    }
////                    MainLogger.getLogger().warning("Invalid parameter or wrong format supplied for placeholder '$s' ($param)")
//                    return@forEach
//                }
//
//                if (paramEntry[0].isEmpty() || paramEntry[1].isEmpty()) {
//                    return@forEach
//                }
//
//                params[paramEntry[0]] = PlaceholderParameters.Parameter(paramEntry[0], paramEntry[1].replace('&', '§'))
//            }
//        }
//
//        list.add(MatchedGroup(s, matcher.start(), matcher.end(), PlaceholderParameters(params, unnamedParams)))
//    }
//
//    return list
//}

data class MatchedGroup(val value: String, val start: Int, val end: Int, val params: PlaceholderParameters = PlaceholderParameters.EMPTY)
