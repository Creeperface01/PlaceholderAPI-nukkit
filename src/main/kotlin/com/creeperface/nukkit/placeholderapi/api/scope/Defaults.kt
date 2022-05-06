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
        build("player") {
            visitorLoader { player.name }
            aliases("playername")
        }

        build("player_displayname") {
            visitorLoader { player.displayName }
        }

        build("player_uuid") {
            visitorLoader { player.uniqueId }
        }
        build("player_ping") {
            visitorLoader { player.ping }
        }
        build("player_level") {
            visitorLoader { player.level?.name }
        }
        build("player_can_fly") {
            visitorLoader { player.adventureSettings.get(AdventureSettings.Type.ALLOW_FLIGHT) }
        }
        build("player_flying") {
            visitorLoader { player.adventureSettings.get(AdventureSettings.Type.FLYING) }
        }

        build("player_health") {
            visitorLoader { player.health }
        }
        build("player_max_health") {
            visitorLoader { player.maxHealth }
        }
        build("player_saturation") {
            visitorLoader { player.foodData.foodSaturationLevel }
        }
        build("player_food") {
            visitorLoader { player.foodData.level }
        }
        build("player_gamemode") {
            visitorLoader { Server.getGamemodeString(player.gamemode) }
        }

        build("player_x") {
            visitorLoader { player.x }
        }
        build("player_y") {
            visitorLoader { player.y }
        }
        build("player_z") {
            visitorLoader { player.z }
        }
        build("player_direction") {
            visitorLoader { player.direction.name }
        }
        build("player_exp") {
            visitorLoader { player.experience }
        }

        build("player_exp_to_next") {
            visitorLoader { Player.calculateRequireExperience(player.experienceLevel + 1) }
        }
        build("player_exp_level") {
            visitorLoader { player.experienceLevel }
        }
        build("player_speed") {
            visitorLoader { player.movementSpeed }
        }
        build("player_max_air") {
            visitorLoader { player.getDataPropertyInt(Entity.DATA_MAX_AIR) }
        }
        build("player_remaining_air") {
            visitorLoader { player.getDataPropertyInt(Entity.DATA_AIR) }
        }
        build("player_item_in_hand") {
            visitorLoader { player.inventory?.itemInHand?.name }
        }

        val server = this.server
        val runtime = Runtime.getRuntime()

        build("server_online") {
            loader {
                server.onlinePlayers.size
            }
        }

        build("server_max_players") {
            loader {
                server.maxPlayers
            }
        }

        build("server_motd") {
            loader {
                server.network.name
            }
        }

        build("server_ram_used") {
            loader {
                (runtime.totalMemory() - runtime.freeMemory()).bytes2MB().round(configuration.coordsAccuracy)
            }
        }

        build("server_ram_free") {
            loader {
                runtime.freeMemory().bytes2MB().round(configuration.coordsAccuracy)
            }
        }

        build("server_ram_total") {
            loader {
                runtime.totalMemory().bytes2MB().round(configuration.coordsAccuracy)
            }
        }

        build("server_ram_max") {
            loader {
                runtime.maxMemory().bytes2MB().round(configuration.coordsAccuracy)
            }
        }

        build("server_cores") {
            loader {
                runtime.availableProcessors()
            }
        }

        build("server_tps") {
            loader {
                server.ticksPerSecondAverage
            }
        }

        build("server_uptime") {
            loader {
                Duration.ofMillis(System.currentTimeMillis() - Nukkit.START_TIME)
            }
        }

        build("time") {
            loader { Date() }
        }

        //scoped
        build("message") {
            scopedLoader(ChatScope) {
                contextVal.message
            }
        }

        build("message_sender") {
            scopedLoader(ChatScope) {
                contextVal.player
            }
        }

        build("message") {
            scopedLoader(MessageScope) {
                contextVal.message
            }
        }

        build("message_sender") {
            scopedLoader(MessageScope) {
                contextVal.sender
            }
        }
    }
}