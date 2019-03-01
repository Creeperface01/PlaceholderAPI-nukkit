package com.creeperface.nukkit.placeholderapi.command

import cn.nukkit.Player
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import com.creeperface.nukkit.placeholderapi.api.PlaceholderAPI

/**
 * @author CreeperFace
 */
class PlaceholderCommand : Command("placeholder") {

    init {
        permission = "placeholderapi.command"
    }

    override fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) return false

        val message = args.joinToString(" ")

        val p = sender as? Player

        val value = PlaceholderAPI.getInstance().translateString(message, p)

        sender.sendMessage("value: $value")
        return true
    }
}