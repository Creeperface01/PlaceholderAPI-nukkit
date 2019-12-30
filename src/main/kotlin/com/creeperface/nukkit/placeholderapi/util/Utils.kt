package com.creeperface.nukkit.placeholderapi.util

import com.creeperface.nukkit.placeholderapi.PlaceholderAPIIml
import com.google.common.base.Preconditions
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

/**
 * @author CreeperFace
 */

fun String.trimSides(startOffset: Int, endOffset: Int) = this.substring(startOffset, length - endOffset)

fun <T : Number> T.round(accuracy: Int = 2): T {
    Preconditions.checkArgument(accuracy >= 0)

    val i = 10.toDouble().pow(accuracy.toDouble())

    @Suppress("UNCHECKED_CAST")
    return ((this.toDouble() * i).roundToInt() / i) as T
}

fun Long.formatAsTime(format: String): String {
//    val time = this / 1000
//
//    val seconds = NukkitMath.floorDouble((time % 60L).toDouble())
//    val minutes = NukkitMath.floorDouble((time % 3600L / 60L).toDouble())
//    val hours = NukkitMath.floorDouble((time / 3600L).toDouble())
//
//    return format.replace("ss", seconds.toString()).replace("mm", minutes.toString()).replace("HH", hours.toString())
    return SimpleDateFormat(format).format(Date(this))
}

fun Long.bytes2MB() = this / 1024.0 / 1024.0

fun Boolean.toFormatString(): String {
    val conf = PlaceholderAPIIml.instance.configuration

    return if (this) conf.booleanTrueFormat else conf.booleanFalseFormat
}

fun Date.toFormatString(): String {
    return SimpleDateFormat(PlaceholderAPIIml.instance.configuration.dateFormat).format(this)
}

fun Any.toFormattedString(): String = when (this) {
    is Boolean -> toFormatString()
    is Date -> toFormatString()
    else -> toString()
}

fun KClass<*>.nestedSuperClass(clazz: KClass<*>, level: Int = 0): Int {
    if (this == clazz) {
        return level
    }

    if (this.superclasses.isEmpty()) {
        return -1
    }

    var minLevel = Int.MAX_VALUE

    this.superclasses.forEach { superClass ->
        val superLevel = superClass.nestedSuperClass(clazz, level + 1)

        if (superLevel in 0 until minLevel) {
            minLevel = superLevel
        }
    }

    return minLevel
}