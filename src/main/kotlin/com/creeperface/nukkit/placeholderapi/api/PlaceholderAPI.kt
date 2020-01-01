package com.creeperface.nukkit.placeholderapi.api

import cn.nukkit.Player
import com.creeperface.nukkit.placeholderapi.PlaceholderAPIIml
import com.creeperface.nukkit.placeholderapi.api.scope.GlobalScope
import com.creeperface.nukkit.placeholderapi.api.scope.Scope
import com.creeperface.nukkit.placeholderapi.api.util.*
import java.util.*
import java.util.function.BiFunction
import java.util.function.Function
import kotlin.reflect.KClass

/**
 * @author CreeperFace
 */
@Suppress("DEPRECATION", "UNUSED", "UNCHECKED_CAST")
abstract class PlaceholderAPI internal constructor() {

    abstract val globalScope: AnyScope

    abstract fun <T : Any> staticPlaceholder(
            name: String,
            typeClass: KClass<T>,
            loader: (PlaceholderParameters, AnyContext) -> T?,
            updateInterval: Int = -1,
            autoUpdate: Boolean = false,
            processParameters: Boolean = false,
            scope: AnyScopeClass = GlobalScope::class,
            vararg aliases: String
    )

    abstract fun <T : Any> visitorSensitivePlaceholder(
            name: String,
            typeClass: KClass<T>,
            loader: (Player, PlaceholderParameters, AnyContext) -> T?,
            updateInterval: Int = -1,
            autoUpdate: Boolean = false,
            processParameters: Boolean = false,
            scope: AnyScopeClass = GlobalScope::class,
            vararg aliases: String
    )

    abstract fun registerPlaceholder(placeholder: AnyPlaceholder)

    fun getPlaceholder(key: String) = getPlaceholder(key, GlobalScope)

    abstract fun getPlaceholder(key: String, scope: AnyScope = GlobalScope): AnyPlaceholder?

    fun getPlaceholders() = getPlaceholders(GlobalScope)

    abstract fun getPlaceholders(scope: AnyScope = GlobalScope): PlaceholderGroup

    fun getValue(key: String) = getValue(key, null)

    fun getValue(key: String, visitor: Player?) = getValue(key, visitor, key)

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

    abstract fun getValue(
            key: String,
            visitor: Player? = null,
            defaultValue: String? = key,
            params: PlaceholderParameters = PlaceholderParameters.EMPTY,
            context: AnyContext = GlobalScope.defaultContext
    ): String?

    fun updatePlaceholder(key: String) = updatePlaceholder(key, null)

    fun updatePlaceholder(key: String, visitor: Player?) = updatePlaceholder(key, visitor, GlobalScope.defaultContext)

    abstract fun updatePlaceholder(key: String, visitor: Player?, context: AnyContext)

    fun translateString(input: String) = translateString(input, null)

    fun translateString(
            input: String,
            visitor: Player? = null
    ) = translateString(input, visitor, GlobalScope.defaultContext, input.matchPlaceholders())

    fun translateString(
            input: String,
            visitor: Player? = null,
            context: AnyContext = GlobalScope.defaultContext
    ) = translateString(input, visitor, context, input.matchPlaceholders())

    abstract fun translateString(input: String, visitor: Player?, context: AnyContext, matched: Collection<MatchedGroup>): String

    fun findPlaceholders(input: String, scope: AnyScope = GlobalScope) = findPlaceholders(input.matchPlaceholders(), scope)

    abstract fun findPlaceholders(matched: Collection<MatchedGroup>, scope: AnyScope = GlobalScope): List<AnyPlaceholder>

    abstract fun formatTime(millis: Long): String

    fun formatDate(date: Date) = formatDate(date.time)

    abstract fun formatDate(millis: Long): String

    @Deprecated("formatBoolean() was replaced by a more generic function", ReplaceWith("formatObject(value)"))
    abstract fun formatBoolean(value: Boolean): String

    abstract fun formatObject(value: Any?): String

    abstract fun <T : Any> registerFormatter(clazz: KClass<T>, formatFun: (T) -> String)

    fun <T : Any> registerFormatter(clazz: Class<T>, formatFun: Function<T, String>) = registerFormatter(clazz.kotlin) { formatFun.apply(it) }

    abstract fun getFormatter(clazz: KClass<*>): PFormatter

    fun getFormatter(obj: Any) = getFormatter(obj::class)

    fun getFormatter(clazz: Class<*>) = getFormatter(clazz.kotlin)

    inline fun <reified T : Any> buildStatic(name: String, noinline loader: (PlaceholderParameters) -> T?) = StaticBuilder(
            name,
            T::class
    ) { params: PlaceholderParameters, _: AnyContext -> loader(params) }

    fun <T : Any> buildStatic(typeClass: Class<T>, name: String, loader: Function<PlaceholderParameters, T?>) = StaticBuilder(
            name,
            typeClass.kotlin
    ) { params: PlaceholderParameters, _: AnyContext -> loader.apply(params) }

    inline fun <reified T : Any, ST, reified S : Scope<ST, S>> buildStatic(name: String, noinline loader: (PlaceholderParameters, Scope<ST, S>.Context) -> T?) = StaticBuilder(
            name,
            T::class,
            S::class,
            loader as (PlaceholderParameters, AnyContext) -> T?
    )

    fun <T : Any, ST, S : Scope<ST, S>> buildStatic(typeClass: Class<T>, scopeClass: Class<S>, name: String, loader: BiFunction<PlaceholderParameters, Scope<ST, S>.Context, T?>) = StaticBuilder(
            name,
            typeClass.kotlin,
            scopeClass.kotlin,
            { params: PlaceholderParameters, context: Scope<ST, S>.Context -> loader.apply(params, context) } as (PlaceholderParameters, AnyContext) -> T?
    )

    inline fun <reified T : Any> buildVisitorSensitive(name: String, noinline loader: (Player, PlaceholderParameters) -> T?) = VisitorBuilder(
            name,
            T::class
    ) { player: Player, params: PlaceholderParameters, _: AnyContext -> loader(player, params) }

    fun <T : Any> buildVisitorSensitive(typeClass: Class<T>, name: String, loader: BiFunction<Player, PlaceholderParameters, T?>) = VisitorBuilder(
            name,
            typeClass.kotlin
    ) { player: Player, params: PlaceholderParameters, _: AnyContext -> loader.apply(player, params) }

    inline fun <reified T : Any, ST, reified S : Scope<ST, S>> buildVisitorSensitive(name: String, noinline loader: (Player, PlaceholderParameters, Scope<ST, S>.Context) -> T?) = VisitorBuilder(
            name,
            T::class,
            S::class,
            loader as (Player, PlaceholderParameters, AnyContext) -> T?
    )

    fun <T : Any, ST, S : Scope<ST, S>> buildVisitorSensitive(typeClass: Class<T>, scopeClass: Class<S>, name: String, loader: TriFunction<Player, PlaceholderParameters, Scope<ST, S>.Context, T?>) = VisitorBuilder(
            name,
            typeClass.kotlin,
            scopeClass.kotlin,
            { player: Player, params: PlaceholderParameters, context: Scope<ST, S>.Context ->
                loader.apply(player, params, context)
            } as (Player, PlaceholderParameters, AnyContext) -> T?
    )

    class StaticBuilder<T : Any> constructor(
            name: String,
            typeClass: KClass<T>,
            scopeClass: AnyScopeClass = GlobalScope::class,
            private val loader: (PlaceholderParameters, scopeContext: AnyContext) -> T?
    ) : Builder<T, StaticBuilder<T>>(name, typeClass, scopeClass) {

        override fun build() {
            getInstance().staticPlaceholder(
                    name,
                    typeClass,
                    loader,
                    updateInterval,
                    autoUpdate,
                    processParameters,
                    scopeClass,
                    *aliases
            )
        }
    }

    class VisitorBuilder<T : Any> constructor(
            name: String,
            typeClass: KClass<T>,
            scopeClass: AnyScopeClass = GlobalScope::class,
            private val loader: (Player, PlaceholderParameters, AnyContext) -> T?
    ) : Builder<T, VisitorBuilder<T>>(name, typeClass, scopeClass) {

        override fun build() {
            getInstance().visitorSensitivePlaceholder(
                    name,
                    typeClass,
                    loader,
                    updateInterval,
                    autoUpdate,
                    processParameters,
                    scopeClass,
                    *aliases
            )
        }

    }

    @Suppress("UNCHECKED_CAST")
    abstract class Builder<T : Any, B : Builder<T, B>> internal constructor(
            val name: String,
            val typeClass: KClass<T>,
            val scopeClass: AnyScopeClass
    ) {

        private val self by lazy { this as B }

        protected var updateInterval = -1
        protected var autoUpdate = false
        protected var aliases = emptyArray<String>()
        protected var processParameters = false

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