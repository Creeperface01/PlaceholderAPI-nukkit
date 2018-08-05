package com.creeperface.nukkit.placeholderapi.api.function

/**
 * @author CreeperFace
 */
@FunctionalInterface
interface Supplier<out R> : Function<R> {

    operator fun invoke(): R
}