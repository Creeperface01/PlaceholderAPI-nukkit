package com.creeperface.nukkit.placeholderapi.api.scope

import com.creeperface.nukkit.placeholderapi.api.Placeholder

abstract class Scope<T> {

    open val global = false

    open val parent: Scope<*>? = GlobalScope

    val placeholders = mutableMapOf<String, Placeholder<out Any?>>()

    open val defaultContext: Context
        get() = throw UnsupportedOperationException("Scope ${this::class.java.name} doesn't have default scope")

    open fun hasDefaultContext() = false

    inner class Context(val context: T, val scope: Scope<T> = this, parentContext: Context? = null) {

        val parentContext: Scope<*>.Context?

        init {
            this.parentContext = when (parentContext) {
                null -> {
                    scope.parent.let { parentScope ->
                        if (parentScope == null) {
                            return@let null
                        }

                        if (parentScope.hasDefaultContext()) {
                            parentScope.defaultContext
                        } else {
                            null
                        }
                    }
                }
                else -> parentContext
            }
        }
    }
}