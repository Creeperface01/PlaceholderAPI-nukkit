package com.creeperface.nukkit.placeholderapi

import cn.nukkit.plugin.Plugin
import cn.nukkit.utils.SimpleConfig

/**
 * @author CreeperFace
 */
class Configuration(plugin: Plugin) : SimpleConfig(plugin) {

    var version = 0.toDouble()
        private set

    @Path("min_update_interval")
    var updateInterval = 10
        private set

    @Path("date_format")
    var dateFormat = "yyyy-MM-dd"
        private set

    @Path("time_format")
    var timeFormat = "HH:mm:ss"
        private set

    @Path("coordinates_accuracy")
    var coordsAccuracy = 2
        private set
}