package com.creeperface.nukkit.placeholderapi

import cn.nukkit.plugin.PluginBase

/**
 * @author CreeperFace
 */
class PlaceholderPlugin : PluginBase() {

    private lateinit var api: PlaceholderAPIIml

    override fun onLoad() {
        api = PlaceholderAPIIml.createInstance(this)
    }

    override fun onEnable() {
        api.init()
    }
}