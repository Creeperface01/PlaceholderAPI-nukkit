package com.creeperface.nukkit.placeholderapi.command

import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import com.creeperface.nukkit.placeholderapi.PlaceholderPlugin
import com.creeperface.nukkit.placeholderapi.api.PlaceholderAPI

/**
 * @author CreeperFace
 */
class PlaceholderCommand(private val plugin: PlaceholderPlugin) : Command("placeholder") {

    override fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) return false

        val placeholder = args[0]
        var player: String? = null

        if (args.size > 1) {
            player = args[1]
        }

        val p = if (player != null) plugin.server.getPlayer(player) else null

        val value = PlaceholderAPI.getInstance().getValue(placeholder, p)

        sender.sendMessage("value: $value")
        return true
    }
}