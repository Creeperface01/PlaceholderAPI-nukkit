package com.creeperface.nukkit.placeholderapi

import cn.nukkit.plugin.PluginBase
import cn.nukkit.utils.PluginException
import com.creeperface.nukkit.placeholderapi.util.KotlinLibDownloader

/**
 * @author CreeperFace
 */
class PlaceholderPlugin : PluginBase() {

    override fun onLoad() {
        if (!KotlinLibDownloader.check(this)) {
            throw PluginException("KotlinLib could not be found")
        }

        PlaceholderAPIIml.createInstance(this)
    }

    override fun onEnable() {
        (PlaceholderAPIIml.instance as PlaceholderAPIIml).init()
    }
}