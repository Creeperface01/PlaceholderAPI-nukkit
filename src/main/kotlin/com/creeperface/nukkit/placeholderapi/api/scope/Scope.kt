package com.creeperface.nukkit.placeholderapi.api.scope

import com.creeperface.nukkit.placeholderapi.api.Placeholder

abstract class Scope {

    open val global = false

    open val parent: Scope? = GlobalScope

    val placeholders = mutableMapOf<String, Placeholder<out Any?>>()
}