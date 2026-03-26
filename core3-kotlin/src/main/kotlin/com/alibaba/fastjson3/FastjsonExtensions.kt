@file:JvmName("FastjsonKt")

package com.alibaba.fastjson3

// ==================== Parse Extensions ====================

/**
 * Parse JSON string to typed object.
 *
 * ```kotlin
 * val user: User? = jsonStr.parseAs<User>()
 * ```
 */
inline fun <reified T> String.parseAs(): T? =
    JSON.parseObject(this, T::class.java)

/**
 * Parse JSON string to typed object with configuration preset.
 *
 * ```kotlin
 * val user: User? = jsonStr.parseAs<User>(ParseConfig.LENIENT)
 * ```
 */
inline fun <reified T> String.parseAs(config: ParseConfig): T? =
    JSON.parse(this, T::class.java, config)

/**
 * Parse JSON bytes (UTF-8) to typed object.
 *
 * ```kotlin
 * val user: User? = bytes.parseAs<User>()
 * ```
 */
inline fun <reified T> ByteArray.parseAs(): T? =
    JSON.parseObject(this, T::class.java)

/**
 * Parse JSON string to `List<T>`.
 *
 * ```kotlin
 * val users: List<User>? = jsonStr.parseList<User>()
 * ```
 */
inline fun <reified T> String.parseList(): List<T>? =
    JSON.parseArray(this, T::class.java)

/**
 * Parse JSON string to `Set<T>`.
 *
 * ```kotlin
 * val tags: Set<String>? = jsonStr.parseSet<String>()
 * ```
 */
inline fun <reified T> String.parseSet(): Set<T>? =
    JSON.parseSet(this, T::class.java)

/**
 * Parse JSON string to `Map<String, T>`.
 *
 * ```kotlin
 * val users: Map<String, User>? = jsonStr.parseMap<User>()
 * ```
 */
inline fun <reified T> String.parseMap(): Map<String, T>? =
    JSON.parseMap(this, T::class.java)

// ==================== Serialize Extensions ====================

/**
 * Serialize any object to JSON string.
 *
 * ```kotlin
 * val json: String = user.toJSON()
 * ```
 */
fun Any?.toJSON(): String = JSON.toJSONString(this)

/**
 * Serialize any object to UTF-8 JSON bytes.
 *
 * ```kotlin
 * val bytes: ByteArray = user.toJSONBytes()
 * ```
 */
fun Any?.toJSONBytes(): ByteArray = JSON.toJSONBytes(this)

/**
 * Serialize any object to pretty-formatted JSON string.
 *
 * ```kotlin
 * val pretty: String = user.toJSONPretty()
 * ```
 */
fun Any?.toJSONPretty(): String = JSON.writePretty(this)

// ==================== JSONObject Extensions ====================

/**
 * Get a typed value from JSONObject with automatic conversion.
 *
 * ```kotlin
 * val user: User? = jsonObj.getAs<User>("user")
 * val age: Int? = jsonObj.getAs<Int>("age")
 * ```
 */
inline fun <reified T> JSONObject.getAs(key: String): T? {
    val value = this[key] ?: return null
    if (value is T) return value
    return com.alibaba.fastjson3.JSON.toJavaObject(value, T::class.java)
}

// ==================== JSONArray Extensions ====================

/**
 * Convert JSONArray to a typed list.
 *
 * ```kotlin
 * val users: List<User> = jsonArr.toList<User>()
 * ```
 */
inline fun <reified T> JSONArray.toList(): List<T> =
    map { JSON.toJavaObject(it, T::class.java) }
