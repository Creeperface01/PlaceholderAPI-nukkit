package com.creeperface.nukkit.placeholderapi.api.scope

object GlobalScope : Scope<Any?>() {

    override val global = true

    override val parent: Nothing? = null

    override val defaultContext = Context(null)

    override fun hasDefaultContext() = true
}