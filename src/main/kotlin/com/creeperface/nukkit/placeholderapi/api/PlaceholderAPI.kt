package com.creeperface.nukkit.placeholderapi.api

import cn.nukkit.Player
import com.creeperface.nukkit.placeholderapi.PlaceholderAPIIml
import com.creeperface.nukkit.placeholderapi.api.Placeholder.VisitorEntry
import com.creeperface.nukkit.placeholderapi.api.scope.GlobalScope
import com.creeperface.nukkit.placeholderapi.api.scope.Scope
import com.creeperface.nukkit.placeholderapi.api.util.*
import java.util.*
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
            loader: Loader<T>,
            updateInterval: Int = -1,
            autoUpdate: Boolean = false,
            processParameters: Boolean = false,
            scope: AnyScopeClass = GlobalScope::class,
            vararg aliases: String
    )

    abstract fun <T : Any> visitorSensitivePlaceholder(
            name: String,
            typeClass: KClass<T>,
            loader: Loader<T>,
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
            vararg contexts: AnyContext = arrayOf(GlobalScope.defaultContext)
    ): String?

    fun updatePlaceholder(key: String) = updatePlaceholder(key, null)

    fun updatePlaceholder(key: String, visitor: Player?) = updatePlaceholder(key, visitor, GlobalScope.defaultContext)

    abstract fun updatePlaceholder(key: String, visitor: Player?, context: AnyContext)

    fun translateString(input: String) = translateString(input, null)

    fun translateString(
            input: String,
            visitor: Player? = null
    ) = translateString(input, visitor, input.matchPlaceholders(), GlobalScope.defaultContext)

    fun translateString(
            input: String,
            visitor: Player? = null,
            vararg contexts: AnyContext = arrayOf(GlobalScope.defaultContext)
    ) = translateString(input, visitor, input.matchPlaceholders(), *contexts)

    abstract fun translateString(input: String, visitor: Player?, matched: Collection<MatchedGroup>, vararg contexts: AnyContext): String

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

//    inline fun <reified T : Any> buildStatic(name: String, noinline loader: Loader<T>) = StaticBuilder(
//            name,
//            T::class,
//            loader = loader
//    )
//
//    fun <T : Any> buildStatic(typeClass: Class<T>, name: String, loader: Function<PlaceholderParameters, T?>) = StaticBuilder(
//            name,
//            typeClass.kotlin
//    ) { loader.apply(parameters) }
//
//    inline fun <reified T : Any, ST, reified S : Scope<ST, S>> buildStaticScoped(name: String, noinline loader: ScopedLoader<ST, S, T>) = StaticBuilder(
//            name,
//            T::class,
//            S::class,
//            loader as Loader<T>
//    )
//
//    fun <T : Any, ST, S : Scope<ST, S>> buildStaticScoped(typeClass: Class<T>, scopeClass: Class<S>, name: String, loader: BiFunction<PlaceholderParameters, Scope<ST, S>.Context, T?>) = StaticBuilder(
//            name,
//            typeClass.kotlin,
//            scopeClass.kotlin
//    ) { loader.apply(parameters, context as Scope<ST, S>.Context) }
//
//    inline fun <reified T : Any> buildVisitorSensitive(name: String, noinline loader: VisitorLoader<T>) = VisitorBuilder(
//            name,
//            T::class,
//            loader = loader as Loader<T>
//    )
//
//    fun <T : Any> buildVisitorSensitive(typeClass: Class<T>, name: String, loader: BiFunction<Player, PlaceholderParameters, T?>) = VisitorBuilder(
//            name,
//            typeClass.kotlin
//    ) { loader.apply(player!!, parameters) }
//
//    inline fun <reified T : Any, ST, reified S : Scope<ST, S>> buildVisitorSensitiveScoped(name: String, noinline loader: VisitorScopedLoader<ST, S, T>) = VisitorBuilder(
//            name,
//            T::class,
//            S::class,
//            loader as Loader<T>
//    )
//
//    fun <T : Any, ST, S : Scope<ST, S>> buildVisitorSensitiveScoped(typeClass: Class<T>, scopeClass: Class<S>, name: String, loader: TriFunction<Player, PlaceholderParameters, Scope<ST, S>.Context, T?>) = VisitorBuilder(
//            name,
//            typeClass.kotlin,
//            scopeClass.kotlin
//    ) { loader.apply(player!!, parameters, context as Scope<ST, S>.Context) }
//
//    class StaticBuilder<T : Any> constructor(
//            name: String,
//            typeClass: KClass<T>,
//            scopeClass: AnyScopeClass = GlobalScope::class,
//            private val loader: AnyValueEntry<T>.() -> T?
//    ) : Builder<T, StaticBuilder<T>>(name, typeClass, scopeClass) {
//
//        override fun build() {
//            getInstance().staticPlaceholder(
//                    name,
//                    typeClass,
//                    loader,
//                    updateInterval,
//                    autoUpdate,
//                    processParameters,
//                    scopeClass,
//                    *aliases
//            )
//        }
//    }
//
//    class VisitorBuilder<T : Any> constructor(
//            name: String,
//            typeClass: KClass<T>,
//            scopeClass: AnyScopeClass = GlobalScope::class,
//            private val loader: AnyValueEntry<T>.() -> T?
//    ) : Builder<T, VisitorBuilder<T>>(name, typeClass, scopeClass) {
//
//        override fun build() {
//            getInstance().visitorSensitivePlaceholder(
//                    name,
//                    typeClass,
//                    loader,
//                    updateInterval,
//                    autoUpdate,
//                    processParameters,
//                    scopeClass,
//                    *aliases
//            )
//        }
//
//    }

    fun <T : Any> builder(name: String, typeClass: Class<T>) = Builder(name, typeClass.kotlin)

    inline fun <reified T : Any> build(name: String, builder: Builder<T>.() -> Unit) {
        Builder(name, T::class).let {
            builder(it)
            it.build()
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Builder<T : Any> constructor(
            private val name: String,
            private val typeClass: KClass<T>
    ) {

        private var updateInterval = -1
        private var autoUpdate = false
        private var aliases = emptyArray<String>()
        private var processParameters = false

        private var built = false
        private var loader: Loader<T>? = null
        private var scopeClass: AnyScopeClass? = null
        private var visitor = false

        fun loader(loader: Loader<T>): Builder<T> {
            this.loader = loader
            visitor = false

            return this
        }

        fun visitorLoader(loader0: VisitorLoader<T>): Builder<T> {
            this.loader = { loader0(this as AnyVisitorValueEntry<T>) }
            visitor = true

            return this
        }

        fun <ST, S : Scope<ST, S>> scopedLoader(scope: S, loader0: ScopedLoader<ST, S, T>): Builder<T> {
            scopeClass = scope::class
            this.loader = { loader0(this as ValueEntry<T, ST, S>, null) }
            visitor = false

            return this
        }

        fun <ST, S : Scope<ST, S>> visitorScopedLoader(scope: S, loader0: VisitorScopedLoader<ST, S, T>): Builder<T> {
            scopeClass = scope::class
            this.loader = { loader0(this as VisitorEntry<T, ST, S>, null) }
            visitor = true

            return this
        }

        fun updateInterval(updateInterval: Int): Builder<T> {
            this.updateInterval = updateInterval
            return this
        }

        fun autoUpdate(autoUpdate: Boolean): Builder<T> {
            this.autoUpdate = autoUpdate
            return this
        }

        fun aliases(vararg aliases: String): Builder<T> {
            this.aliases = arrayOf(*aliases)
            return this
        }

        fun processParameters(processParameters: Boolean): Builder<T> {
            this.processParameters = processParameters
            return this
        }

        fun build() {
            if (built) {
                return
            }
            built = true

            if (visitor) {
                getInstance().visitorSensitivePlaceholder(
                    name,
                    typeClass,
                    loader ?: error("You must set placeholder loader before building"),
                    updateInterval,
                    autoUpdate,
                    processParameters,
                    scopeClass ?: GlobalScope::class,
                    *aliases
                )
            } else {
                getInstance().staticPlaceholder(
                    name,
                    typeClass,
                    loader ?: error("You must set placeholder loader before building"),
                    updateInterval,
                    autoUpdate,
                    processParameters,
                    scopeClass ?: GlobalScope::class,
                    *aliases
                )
            }
        }
    }

    companion object {

        @JvmStatic
        fun getInstance(): PlaceholderAPI {
            return PlaceholderAPIIml.instance
        }
    }
}