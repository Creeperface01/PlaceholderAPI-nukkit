package com.creeperface.nukkit.placeholderapi.api.scope

import com.creeperface.nukkit.placeholderapi.api.Placeholder

abstract class Scope<T> {

    open val global = false

    open val parent: Scope<*>? = GlobalScope

    val placeholders = mutableMapOf<String, Placeholder<out Any?>>()

    open val defaultContext: Context
        get() = throw UnsupportedOperationException("Scope ${this::class.java.name} doesn't have default scope")

    open fun hasDefaultScope() = false

    inner class Context(val context: T, val scope: Scope<T> = this)
}