package com.creeperface.nukkit.placeholderapi.api.scope

import com.creeperface.nukkit.placeholderapi.api.util.AnyContext
import com.creeperface.nukkit.placeholderapi.api.util.AnyPlaceholder
import com.creeperface.nukkit.placeholderapi.api.util.AnyScope

abstract class Scope<T, S : Scope<T, S>> {

    open val global = false

    open val parent: AnyScope? = GlobalScope

    val placeholders = mutableMapOf<String, AnyPlaceholder>()

    open val defaultContext: Context
        get() = throw UnsupportedOperationException("Scope ${this::class.java.name} doesn't have a default scope")

    open fun hasDefaultContext() = false

    @Suppress("UNCHECKED_CAST")
    @JvmOverloads
    open fun getContext(context: T, parentContext: AnyContext? = null): Context {
        return Context(context, this as S, parentContext)
    }

    open inner class Context(val context: T, val scope: S, parentContext: AnyContext? = null) {

        val parentContext: AnyContext?

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