package com.creeperface.nukkit.placeholderapi.util

import com.creeperface.nukkit.placeholderapi.api.PlaceholderParameters
import com.creeperface.nukkit.placeholderapi.api.util.MatchedGroup

object Parser {

    fun parse(input: String): List<MatchedGroup> {
        val placeholders = mutableListOf<MatchedGroup>()

        var i = 0

        fun parsePlaceholder(): MatchedGroup? {
            val startIndex = i - 1
            val name = StringBuilder()

            val parameters = mutableListOf<PlaceholderParameters.Parameter>()
            val namedParameters = mutableMapOf<String, PlaceholderParameters.Parameter>()

            fun parseParameters(): Boolean {
                var paramName = StringBuilder()
                val value = StringBuilder()

                var named = false
                var paramPlaceholder: MatchedGroup? = null

                while (i < input.length) {
                    val char = input[i++]

                    if (char == '=' && !named) { //named parameter
                        named = true
                        paramName = StringBuilder(value)
                        value.clear()
                        continue
                    }

                    if (char == '%') { //placeholder as parameter
                        if (named && value.isNotEmpty()) {
                            return false
                        }

                        paramPlaceholder = parsePlaceholder()

                        if (paramPlaceholder != null) {
                            continue
                        }
                    } else if (char == '>' || char == ';') { //parameter separator
                        if (paramName.isNotBlank()) {
                            namedParameters[paramName.toString()] = PlaceholderParameters.Parameter(
                                    paramName.toString(),
                                    value.toString(),
                                    paramPlaceholder
                            )
                        } else {
                            parameters.add(PlaceholderParameters.Parameter(
                                    null,
                                    value.toString(),
                                    paramPlaceholder
                            ))
                        }

                        if (char == ';') {
                            paramName.clear()
                            value.clear()
                            named = false
                            continue
                        }

                        return true
                    }

                    if (paramPlaceholder != null) {
                        return false
                    }

                    value.append(char)
                }

                return false
            }

            while (i < input.length) {
                val char = input[i++]

                if (char == '%') {
                    if (name.isBlank()) {
                        return null
                    }

                    return MatchedGroup(name.toString(), startIndex, i + 1, PlaceholderParameters(namedParameters, parameters))
                }

                if (char == '<') {
                    if (!parseParameters()) {
                        return null
                    }

                    continue
                }

                name.append(char)
            }

            return null
        }

        while (i < input.length) {
            val char = input[i++]

            if (char == '%') {
                val placeholder = parsePlaceholder() ?: return placeholders

                placeholders.add(placeholder)
                continue
            }
        }

        return placeholders
    }
}