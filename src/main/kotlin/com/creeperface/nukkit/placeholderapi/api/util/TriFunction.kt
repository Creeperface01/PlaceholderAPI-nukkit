package com.creeperface.nukkit.placeholderapi.api.util

@FunctionalInterface
interface TriFunction<T, U, V, R> {

    fun apply(t: T, u: U, v: V): R
}