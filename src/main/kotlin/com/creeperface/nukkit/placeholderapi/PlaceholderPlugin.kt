package com.creeperface.nukkit.placeholderapi

import cn.nukkit.plugin.PluginBase
import cn.nukkit.utils.PluginException
import com.creeperface.nukkit.placeholderapi.util.KotlinLibDownloader

/**
 * @author CreeperFace
 */
class PlaceholderPlugin : PluginBase() {

    private lateinit var api: PlaceholderAPIIml

    override fun onLoad() {
        if (!KotlinLibDownloader.check(this)) {
            throw PluginException("KotlinLib could not be found")
        }

        api = PlaceholderAPIIml.createInstance(this)
    }

    override fun onEnable() {
        api.init()
    }
}