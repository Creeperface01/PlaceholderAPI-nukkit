package com.creeperface.nukkit.placeholderapi.api.scope

object GlobalScope : Scope<Any?, GlobalScope>() {

    override val global = true

    override val parent: Nothing? = null

    override val defaultContext = Context(null, this)

    override fun hasDefaultContext() = true
}