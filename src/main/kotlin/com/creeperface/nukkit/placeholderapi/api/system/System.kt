package com.creeperface.nukkit.placeholderapi.api.system

import cn.nukkit.Player

/**
 * @author CreeperFace
 */
interface System { //TODO: attempt to provide some usable API

    fun addPlayer(p: Player)

    fun removePlayer(p: Player)

    fun process()
}