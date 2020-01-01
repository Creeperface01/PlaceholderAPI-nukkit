package com.creeperface.nukkit.placeholderapi.api.util

import com.creeperface.nukkit.placeholderapi.api.Placeholder
import com.creeperface.nukkit.placeholderapi.api.PlaceholderParameters
import com.creeperface.nukkit.placeholderapi.api.scope.Scope
import com.creeperface.nukkit.placeholderapi.util.Parser
import kotlin.reflect.KClass

/**
 * @author CreeperFace
 */
typealias PFormatter = (Any?) -> String

typealias AnyScope = Scope<out Any?, *>
typealias AnyScopeClass = KClass<out Scope<out Any?, *>>

typealias AnyContext = Scope<out Any?, *>.Context
typealias AnyPlaceholder = Placeholder<out Any>

typealias PlaceholderGroup = MutableMap<String, AnyPlaceholder>

fun String.matchPlaceholders() = Parser.parse(this)

data class MatchedGroup(val value: String, val start: Int, val end: Int, val params: PlaceholderParameters = PlaceholderParameters.EMPTY)
