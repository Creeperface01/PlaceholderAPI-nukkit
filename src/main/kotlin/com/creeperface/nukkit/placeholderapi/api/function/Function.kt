package com.creeperface.nukkit.placeholderapi.api.function

/**
 * @author CreeperFace
 */
@FunctionalInterface
interface Function<in T, out R> : Function<R> {
    operator fun invoke(p: T): R
}