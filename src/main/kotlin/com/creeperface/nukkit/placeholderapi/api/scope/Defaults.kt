package com.creeperface.nukkit.placeholderapi.api.scope

import cn.nukkit.AdventureSettings
import cn.nukkit.Nukkit
import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.entity.Entity
import cn.nukkit.event.player.PlayerChatEvent
import com.creeperface.nukkit.placeholderapi.PlaceholderAPIIml
import com.creeperface.nukkit.placeholderapi.util.bytes2MB
import com.creeperface.nukkit.placeholderapi.util.round
import java.time.Duration
import java.util.*

class Message(
        val sender: Player? = null,
        val message: String
)

object MessageScope : Scope<Message, MessageScope>() {

    fun getContext(sender: Player?, message: String): Context {
        return super.getContext(Message(sender, message), null)
    }
}

object ChatScope : Scope<PlayerChatEvent, ChatScope>()

val PlayerChatEvent.context: Scope<PlayerChatEvent, ChatScope>.Context
    get() = ChatScope.getContext(this)

internal fun registerDefaultPlaceholders(api: PlaceholderAPIIml) {
    with(api) {
        build<String>("player") {
            visitorLoader { player.name }
            aliases("playername")
        }

        build<String>("player_displayname") {
            visitorLoader { player.displayName }
        }

        build<UUID>("player_uuid") {
            visitorLoader { player.uniqueId }
        }
        build<Int>("player_ping") {
            visitorLoader { player.ping }
        }
        build<String>("player_level") {
            visitorLoader { player.level?.name }
        }
        build<Boolean>("player_can_fly") {
            visitorLoader { player.adventureSettings.get(AdventureSettings.Type.ALLOW_FLIGHT) }
        }
        build<Boolean>("player_flying") {
            visitorLoader { player.adventureSettings.get(AdventureSettings.Type.FLYING) }
        }

        build<Float>("player_health") {
            visitorLoader { player.health }
        }
        build<Int>("player_max_health") {
            visitorLoader { player.maxHealth }
        }
        build<Float>("player_saturation") {
            visitorLoader { player.foodData.foodSaturationLevel }
        }
        build<Int>("player_food") {
            visitorLoader { player.foodData.level }
        }
        build<String>("player_gamemode") {
            visitorLoader { Server.getGamemodeString(player.gamemode) }
        }

        build<Double>("player_x") {
            visitorLoader { player.x }
        }
        build<Double>("player_y") {
            visitorLoader { player.y }
        }
        build<Double>("player_z") {
            visitorLoader { player.z }
        }
        build<String>("player_direction") {
            visitorLoader { player.direction.name }
        }
        build<Int>("player_exp") {
            visitorLoader { player.experience }
        }

        build<Int>("player_exp_to_next") {
            visitorLoader { Player.calculateRequireExperience(player.experienceLevel + 1) }
        }
        build<Int>("player_exp_level") {
            visitorLoader { player.experienceLevel }
        }
        build<Float>("player_speed") {
            visitorLoader { player.movementSpeed }
        }
        build<Int>("player_max_air") {
            visitorLoader { player.getDataPropertyInt(Entity.DATA_MAX_AIR) }
        }
        build<Int>("player_remaining_air") {
            visitorLoader { player.getDataPropertyInt(Entity.DATA_AIR) }
        }
        build<String>("player_item_in_hand") {
            visitorLoader { player.inventory?.itemInHand?.name }
        }

        val server = this.server
        val runtime = Runtime.getRuntime()

        build<Int>("server_online") {
            loader {
                server.onlinePlayers.size
            }
        }

        build<Int>("server_max_players") {
            loader {
                server.maxPlayers
            }
        }

        build<String>("server_motd") {
            loader {
                server.network.name
            }
        }

        build<Double>("server_ram_used") {
            loader {
                (runtime.totalMemory() - runtime.freeMemory()).bytes2MB().round(configuration.coordsAccuracy)
            }
        }

        build<Double>("server_ram_free") {
            loader {
                runtime.freeMemory().bytes2MB().round(configuration.coordsAccuracy)
            }
        }

        build<Double>("server_ram_total") {
            loader {
                runtime.totalMemory().bytes2MB().round(configuration.coordsAccuracy)
            }
        }

        build<Double>("server_ram_max") {
            loader {
                runtime.maxMemory().bytes2MB().round(configuration.coordsAccuracy)
            }
        }

        build<Int>("server_cores") {
            loader {
                runtime.availableProcessors()
            }
        }

        build<Float>("server_tps") {
            loader {
                server.ticksPerSecondAverage
            }
        }

        build<Duration>("server_uptime") {
            loader {
                Duration.ofMillis(System.currentTimeMillis() - Nukkit.START_TIME)
            }
        }

        build<Date>("time") {
            loader { Date() }
        }

        //scoped
        build<String>("message") {
            scopedLoader(ChatScope) {
                contextVal.message
            }
        }

        build<Player>("message_sender") {
            scopedLoader(ChatScope) {
                contextVal.player
            }
        }

        build<String>("message") {
            scopedLoader(MessageScope) {
                contextVal.message
            }
        }

        build<Player>("message_sender") {
            scopedLoader(MessageScope) {
                contextVal.sender
            }
        }
    }
}