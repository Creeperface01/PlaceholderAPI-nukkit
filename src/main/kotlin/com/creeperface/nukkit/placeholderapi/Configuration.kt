package com.creeperface.nukkit.placeholderapi

import cn.nukkit.plugin.Plugin
import cn.nukkit.utils.SimpleConfig

/**
 * @author CreeperFace
 */
class Configuration(plugin: Plugin) : SimpleConfig(plugin) {

    var version = 0.toDouble()

    @Path("min_update_interval")
    var updateInterval = 10

    @Path("date_format")
    var dateFormat = "yyyy-MM-dd"

    @Path("time_format")
    var timeFormat = "HH:mm:ss"

    @Path("coordinates_accuracy")
    var coordsAccuracy = 2

    @Path("boolean_format.false")
    var booleanFalseFormat = "no"

    @Path("boolean_format.true")
    var booleanTrueFormat = "yes"
}