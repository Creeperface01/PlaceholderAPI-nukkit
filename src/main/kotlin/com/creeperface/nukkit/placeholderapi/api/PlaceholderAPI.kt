package com.creeperface.nukkit.placeholderapi.api

import cn.nukkit.Player
import com.creeperface.nukkit.placeholderapi.PlaceholderAPIIml
import com.creeperface.nukkit.placeholderapi.api.scope.GlobalScope
import com.creeperface.nukkit.placeholderapi.api.util.*
import java.util.*
import java.util.function.BiFunction
import java.util.function.Function

/**
 * @author CreeperFace
 */
@Suppress("DEPRECATION", "UNUSED")
interface PlaceholderAPI {

    val globalScope: AnyScope

    @JvmDefault
    @Deprecated("Use builder instead", ReplaceWith("buildStatic(name, loader)" +
            ".aliases(*aliases)" +
            ".build()"
    ))
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
    @Deprecated("Use builder instead", ReplaceWith("buildStatic(name, loader)" +
            ".updateInterval(updateInterval)" +
            ".build()"
    ))
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

    @Deprecated("Use builder instead", ReplaceWith("buildStatic(name, loader)" +
            ".updateInterval(updateInterval)" +
            ".autoUpdate(autoUpdate)" +
            ".aliases(*aliases)" +
            ".build()"
    ))
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

    @Deprecated("Use builder instead", ReplaceWith("buildStatic(name, loader)" +
            ".updateInterval(updateInterval)" +
            ".autoUpdate(autoUpdate)" +
            ".processParameters(processParameters)" +
            ".aliases(*aliases)" +
            ".build()"
    ))
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

    @Deprecated("Use builder instead", ReplaceWith("buildStatic(name, loader)" +
            ".updateInterval(updateInterval)" +
            ".autoUpdate(autoUpdate)" +
            ".processParameters(processParameters)" +
            ".scope(scope)" +
            ".aliases(*aliases)" +
            ".build()"
    ))
    fun <T> staticPlaceholder(
            name: String,
            loader: Function<PlaceholderParameters, T?>,
            updateInterval: Int = -1,
            autoUpdate: Boolean = false,
            processParameters: Boolean = false,
            scope: AnyScope = GlobalScope,
            vararg aliases: String
    ) where T : Any? = staticPlaceholder(
            name,
            { params, _ -> loader.apply(params) },
            updateInterval,
            autoUpdate, processParameters,
            scope,
            *aliases
    )

    fun <T> staticPlaceholder(
            name: String,
            loader: (PlaceholderParameters, AnyContext) -> T?,
            updateInterval: Int = -1,
            autoUpdate: Boolean = false,
            processParameters: Boolean = false,
            scope: AnyScope = GlobalScope,
            vararg aliases: String
    ) where T : Any?

    @JvmDefault
    @Deprecated("Use builder instead", ReplaceWith("buildVisitorSensitive(name, loader)" +
            ".aliases(*aliases)" +
            ".build()"
    ))
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

    @Deprecated("Use builder instead", ReplaceWith("buildVisitorSensitive(name, loader)" +
            ".updateInterval(updateInterval)" +
            ".build()"
    ))
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

    @Deprecated("Use builder instead", ReplaceWith("buildVisitorSensitive(name, loader)" +
            ".updateInterval(updateInterval)" +
            ".autoUpdate(autoUpdate)" +
            ".aliases(*aliases)" +
            ".build()"
    ))
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

    @Deprecated("Use builder instead", ReplaceWith("buildVisitorSensitive(name, loader)" +
            ".updateInterval(updateInterval)" +
            ".autoUpdate(autoUpdate)" +
            ".processParameters(processParameters)" +
            ".aliases(*aliases)" +
            ".build()"
    ))
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

    @Deprecated("Use builder instead", ReplaceWith("buildVisitorSensitive(name, loader)" +
            ".updateInterval(updateInterval)" +
            ".autoUpdate(autoUpdate)" +
            ".processParameters(processParameters)" +
            ".scope(scope)" +
            ".aliases(*aliases)" +
            ".build()"
    ))
    fun <T> visitorSensitivePlaceholder(
            name: String,
            loader: BiFunction<Player, PlaceholderParameters, T?>,
            updateInterval: Int = -1,
            autoUpdate: Boolean = false,
            processParameters: Boolean = false,
            scope: AnyScope = GlobalScope,
            vararg aliases: String
    ) where T : Any? = visitorSensitivePlaceholder(
            name,
            { player, placeholderParameters, _ -> loader.apply(player, placeholderParameters) },
            updateInterval,
            autoUpdate,
            processParameters,
            scope,
            *aliases
    )

    fun <T> visitorSensitivePlaceholder(
            name: String,
            loader: (Player, PlaceholderParameters, AnyContext) -> T?,
            updateInterval: Int = -1,
            autoUpdate: Boolean = false,
            processParameters: Boolean = false,
            scope: AnyScope = GlobalScope,
            vararg aliases: String
    ) where T : Any?

    fun registerPlaceholder(placeholder: Placeholder<out Any?>)

    @JvmDefault
    fun getPlaceholder(key: String) = getPlaceholder(key, GlobalScope)

    fun getPlaceholder(key: String, scope: AnyScope = GlobalScope): Placeholder<out Any?>?

    @JvmDefault
    fun getPlaceholders() = getPlaceholders(GlobalScope)

    fun getPlaceholders(scope: AnyScope = GlobalScope): PlaceholderGroup

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
    ) = getValue(key, visitor, defaultValue, params, GlobalScope.defaultContext)

    fun getValue(
            key: String,
            visitor: Player? = null,
            defaultValue: String? = key,
            params: PlaceholderParameters = PlaceholderParameters.EMPTY,
            context: AnyContext = GlobalScope.defaultContext
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

    fun findPlaceholders(input: String, scope: AnyScope = GlobalScope) = findPlaceholders(input.matchPlaceholders(), scope)

    fun findPlaceholders(matched: Collection<MatchedGroup>, scope: AnyScope = GlobalScope): List<Placeholder<out Any?>>

    fun formatTime(millis: Long): String

    fun formatDate(date: Date) = formatDate(date.time)

    fun formatDate(millis: Long): String

    fun formatBoolean(value: Boolean): String

    fun <T> buildStatic(name: String, loader: Function<PlaceholderParameters, T?>) = StaticBuilder(
            name,
            loader
    )

    fun <T> buildStatic(name: String, loader: (PlaceholderParameters, AnyContext) -> T?) = StaticBuilder(
            name,
            loader
    )

    fun <T> buildVisitorSensitive(name: String, loader: BiFunction<Player, PlaceholderParameters, T?>) = VisitorBuilder(
            name,
            loader
    )

    fun <T> buildVisitorSensitive(name: String, loader: (Player, PlaceholderParameters, AnyContext) -> T?) = VisitorBuilder(
            name,
            loader
    )

    class StaticBuilder<T> internal constructor(
            name: String,
            private val loader: (PlaceholderParameters, scopeContext: AnyContext) -> T?
    ) : Builder<T, StaticBuilder<T>>(name) {

        internal constructor(
                name: String,
                loader: Function<PlaceholderParameters, T?>
        ) : this(name, { parameters, _ -> loader.apply(parameters) })

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
            private val loader: (Player, PlaceholderParameters, AnyContext) -> T?
    ) : Builder<T, VisitorBuilder<T>>(name) {

        internal constructor(
                name: String,
                loader: BiFunction<Player, PlaceholderParameters, T?>
        ) : this(name, { player, parameters, _ -> loader.apply(player, parameters) })

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
        protected var scope: AnyScope = GlobalScope

        fun updateInterval(updateInterval: Int): B {
            this.updateInterval = updateInterval
            return self
        }

        fun autoUpdate(autoUpdate: Boolean): B {
            this.autoUpdate = autoUpdate
            return self
        }

        fun aliases(vararg aliases: String): B {
            this.aliases = arrayOf(*aliases)
            return self
        }

        fun scope(scope: AnyScope): B {
            this.scope = scope
            return self
        }

        fun processParameters(processParameters: Boolean): B {
            this.processParameters = processParameters
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