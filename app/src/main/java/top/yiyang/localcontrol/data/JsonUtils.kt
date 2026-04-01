package top.yiyang.localcontrol.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

val AppJson = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
    isLenient = true
}

fun JsonObject.stringOrEmpty(vararg keys: String): String {
    for (key in keys) {
        val value = this[key] ?: continue
        if (value !is JsonNull) return value.jsonPrimitive.contentOrNull.orEmpty()
    }
    return ""
}

fun JsonObject.intOrDefault(defaultValue: Int, vararg keys: String): Int {
    for (key in keys) {
        val value = this[key] ?: continue
        value.jsonPrimitive.intOrNull?.let { return it }
    }
    return defaultValue
}

fun JsonObject.booleanOrDefault(defaultValue: Boolean, vararg keys: String): Boolean {
    for (key in keys) {
        val value = this[key] ?: continue
        value.jsonPrimitive.booleanOrNull?.let { return it }
        value.jsonPrimitive.intOrNull?.let { return it != 0 }
    }
    return defaultValue
}

fun parseJsonObject(raw: String): JsonObject? = runCatching {
    AppJson.parseToJsonElement(raw).jsonObject
}.getOrNull()

fun parseJsonMap(raw: String): JsonObject = parseJsonObject(raw) ?: buildJsonObject { }

fun JsonElement.findNestedObject(path: String): JsonElement? {
    if (path.isBlank()) return this
    var current: JsonElement? = this
    path.split(".").forEach { segment ->
        current = current?.jsonObject?.get(segment)
    }
    return current
}

fun jsonPrimitiveOrString(value: Any?): JsonElement = when (value) {
    null -> JsonNull
    is Number -> JsonPrimitive(value)
    is Boolean -> JsonPrimitive(value)
    else -> JsonPrimitive(value.toString())
}

fun JsonObject.firstArrayObject(path: String): JsonObject? {
    val node = if (path.isBlank()) this else findNestedObject(path) ?: this
    return when {
        node is JsonObject -> node
        node?.jsonArray?.firstOrNull() != null -> node.jsonArray.first().jsonObject
        else -> null
    }
}

