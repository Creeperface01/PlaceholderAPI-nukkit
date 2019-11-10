package com.creeperface.nukkit.placeholderapi.api

import cn.nukkit.Player
import com.creeperface.nukkit.placeholderapi.PlaceholderAPIIml
import com.creeperface.nukkit.placeholderapi.api.scope.GlobalScope
import com.creeperface.nukkit.placeholderapi.api.scope.Scope
import com.creeperface.nukkit.placeholderapi.api.util.MatchedGroup
import com.creeperface.nukkit.placeholderapi.api.util.PlaceholderGroup
import com.creeperface.nukkit.placeholderapi.api.util.matchPlaceholders
import java.util.*
import java.util.function.BiFunction
import java.util.function.Function

/**
 * @author CreeperFace
 */
interface PlaceholderAPI {

    val globalScope: Scope

    @JvmDefault
    fun <T> staticPlaceholder(
            name: String,
            loader: Function<PlaceholderParameters, T?>,
            vararg aliases: String
    ) where T : Any? = staticPlaceholder(
            name,
            loader,
            20,
            false,
            *aliases
    )

    @JvmDefault
    fun <T> staticPlaceholder(
            name: String,
            loader: Function<PlaceholderParameters, T?>,
            updateInterval: Int
    ) where T : Any? = staticPlaceholder(
            name,
            loader,
            updateInterval,
            false
    )

    fun <T> staticPlaceholder(
            name: String,
            loader: Function<PlaceholderParameters, T?>,
            updateInterval: Int = -1,
            autoUpdate: Boolean = false,
            vararg aliases: String
    ) where T : Any? = staticPlaceholder(
            name,
            loader,
            updateInterval,
            autoUpdate = false,
            processParameters = false,
            aliases = *aliases
    )

    fun <T> staticPlaceholder(
            name: String,
            loader: Function<PlaceholderParameters, T?>,
            updateInterval: Int = -1,
            autoUpdate: Boolean = false,
            processParameters: Boolean = false,
            vararg aliases: String
    ) where T : Any? = staticPlaceholder(
            name,
            loader,
            updateInterval,
            autoUpdate,
            processParameters,
            GlobalScope,
            *aliases
    )

    fun <T> staticPlaceholder(
            name: String,
            loader: Function<PlaceholderParameters, T?>,
            updateInterval: Int = -1,
            autoUpdate: Boolean = false,
            processParameters: Boolean = false,
            scope: Scope = GlobalScope,
            vararg aliases: String
    ) where T : Any?


    @JvmDefault
    fun <T> visitorSensitivePlaceholder(
            name: String,
            loader: BiFunction<Player, PlaceholderParameters, T?>,
            vararg aliases: String
    ) where T : Any? = visitorSensitivePlaceholder(
            name,
            loader,
            20,
            false,
            *aliases
    )

    fun <T> visitorSensitivePlaceholder(
            name: String,
            loader: BiFunction<Player, PlaceholderParameters, T?>,
            updateInterval: Int
    ) where T : Any? = visitorSensitivePlaceholder(
            name,
            loader,
            updateInterval,
            false
    )

    fun <T> visitorSensitivePlaceholder(
            name: String,
            loader: BiFunction<Player, PlaceholderParameters, T?>,
            updateInterval: Int = -1,
            autoUpdate: Boolean = false,
            vararg aliases: String
    ) where T : Any? = visitorSensitivePlaceholder(
            name,
            loader,
            updateInterval,
            autoUpdate = false,
            processParameters = false
    )

    fun <T> visitorSensitivePlaceholder(
            name: String,
            loader: BiFunction<Player, PlaceholderParameters, T?>,
            updateInterval: Int = -1,
            autoUpdate: Boolean = false,
            processParameters: Boolean = false,
            vararg aliases: String
    ) where T : Any? = visitorSensitivePlaceholder(
            name,
            loader,
            updateInterval,
            autoUpdate,
            processParameters,
            GlobalScope,
            *aliases
    )

    fun <T> visitorSensitivePlaceholder(
            name: String,
            loader: BiFunction<Player, PlaceholderParameters, T?>,
            updateInterval: Int = -1,
            autoUpdate: Boolean = false,
            processParameters: Boolean = false,
            scope: Scope = GlobalScope,
            vararg aliases: String
    ) where T : Any?

    fun registerPlaceholder(placeholder: Placeholder<out Any?>)

    @JvmDefault
    fun getPlaceholder(key: String) = getPlaceholder(key, GlobalScope)

    fun getPlaceholder(key: String, scope: Scope = GlobalScope): Placeholder<out Any?>?

    @JvmDefault
    fun getPlaceholders() = getPlaceholders(GlobalScope)

    fun getPlaceholders(scope: Scope = GlobalScope): PlaceholderGroup

    @JvmDefault
    fun getValue(key: String) = getValue(key, null)

    @JvmDefault
    fun getValue(key: String, visitor: Player?) = getValue(key, visitor, key)

    @JvmDefault
    fun getValue(
            key: String,
            visitor: Player? = null,
            defaultValue: String? = key
    ) = getValue(key, visitor, defaultValue, PlaceholderParameters.EMPTY)

    fun getValue(
            key: String,
            visitor: Player? = null,
            defaultValue: String? = key,
            params: PlaceholderParameters = PlaceholderParameters.EMPTY
    ) = getValue(key, visitor, defaultValue, params, GlobalScope)

    fun getValue(
            key: String,
            visitor: Player? = null,
            defaultValue: String? = key,
            params: PlaceholderParameters = PlaceholderParameters.EMPTY,
            scope: Scope = GlobalScope
    ): String?

    @JvmDefault
    fun updatePlaceholder(key: String) = updatePlaceholder(key, null)

    fun updatePlaceholder(key: String, visitor: Player?)

    @JvmDefault
    fun translateString(input: String) = translateString(input, null)

    @JvmDefault
    fun translateString(
            input: String,
            visitor: Player? = null
    ) = translateString(input, visitor, input.matchPlaceholders())

    fun translateString(input: String, visitor: Player?, matched: Collection<MatchedGroup>): String

    fun findPlaceholders(input: String, scope: Scope = GlobalScope) = findPlaceholders(input.matchPlaceholders(), scope)

    fun findPlaceholders(matched: Collection<MatchedGroup>, scope: Scope = GlobalScope): List<Placeholder<out Any?>>

    fun formatTime(millis: Long): String

    fun formatDate(date: Date) = formatDate(date.time)

    fun formatDate(millis: Long): String

    fun formatBoolean(value: Boolean): String

    fun <T> buildStatic(name: String, loader: Function<PlaceholderParameters, T?>) = StaticBuilder(
            name,
            loader
    )

    fun <T> buildVisitorSensitive(name: String, loader: BiFunction<Player, PlaceholderParameters, T?>) = VisitorBuilder(
            name,
            loader
    )

    class StaticBuilder<T> internal constructor(
            name: String,
            private val loader: Function<PlaceholderParameters, T?>
    ) : Builder<T, StaticBuilder<T>>(name) {

        override fun build() {
            getInstance().staticPlaceholder(
                    name,
                    loader,
                    updateInterval,
                    autoUpdate,
                    processParameters,
                    scope,
                    *aliases
            )
        }
    }

    class VisitorBuilder<T> internal constructor(
            name: String,
            private val loader: BiFunction<Player, PlaceholderParameters, T?>
    ) : Builder<T, StaticBuilder<T>>(name) {

        override fun build() {
            getInstance().visitorSensitivePlaceholder(
                    name,
                    loader,
                    updateInterval,
                    autoUpdate,
                    processParameters,
                    scope,
                    *aliases
            )
        }

    }

    @Suppress("UNCHECKED_CAST")
    abstract class Builder<T, B : Builder<T, B>> internal constructor(
            val name: String
    ) {

        private val self by lazy { this as B }

        protected var updateInterval = -1
        protected var autoUpdate = false
        protected var aliases = emptyArray<String>()
        protected var processParameters = false
        protected var scope: Scope = GlobalScope

        fun setUpdateInterval(updateInterval: Int): B {
            this.updateInterval = updateInterval
            return self
        }

        fun setAutoUpdate(autoUpdate: Boolean): B {
            this.autoUpdate = autoUpdate
            return self
        }

        fun setAliases(vararg aliases: String): B {
            this.aliases = arrayOf(*aliases)
            return self
        }

        fun setScope(scope: Scope): B {
            this.scope = scope
            return self
        }

        abstract fun build()
    }

    companion object {

        @JvmStatic
        fun getInstance(): PlaceholderAPI {
            return PlaceholderAPIIml.instance
        }
    }
}