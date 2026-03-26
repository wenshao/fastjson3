package com.alibaba.fastjson3

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FastjsonExtensionsTest {

    // Use public fields (not data class) for Java reflection compatibility
    class User {
        var name: String = ""
        var age: Int = 0
    }

    private val userJson = """{"name":"Alice","age":30}"""
    private val arrayJson = """[{"name":"Alice","age":30},{"name":"Bob","age":25}]"""

    // ==================== parseAs ====================

    @Test
    fun `parseAs from String`() {
        val user = userJson.parseAs<User>()
        assertNotNull(user)
        assertEquals("Alice", user.name)
        assertEquals(30, user.age)
    }

    @Test
    fun `parseAs from ByteArray`() {
        val user = userJson.toByteArray().parseAs<User>()
        assertNotNull(user)
        assertEquals("Alice", user.name)
    }

    @Test
    fun `parseAs with ParseConfig`() {
        val user = "{'name':'Alice','age':30}".parseAs<User>(ParseConfig.LENIENT)
        assertNotNull(user)
        assertEquals("Alice", user.name)
    }

    @Test
    fun `parseAs null input`() {
        val result = "".parseAs<User>()
        assertNull(result)
    }

    // ==================== parseList / parseSet / parseMap ====================

    @Test
    fun `parseList`() {
        val users = arrayJson.parseList<User>()
        assertNotNull(users)
        assertEquals(2, users.size)
        assertEquals("Alice", users[0].name)
        assertEquals("Bob", users[1].name)
    }

    @Test
    fun `parseSet`() {
        val tags = """["a","b","a"]""".parseSet<String>()
        assertNotNull(tags)
        assertEquals(2, tags.size)
    }

    @Test
    fun `parseMap`() {
        val map = """{"k1":"v1","k2":"v2"}""".parseMap<String>()
        assertNotNull(map)
        assertEquals(2, map.size)
        assertEquals("v1", map["k1"])
    }

    // ==================== toJSON / toJSONBytes / toJSONPretty ====================

    @Test
    fun `toJSON`() {
        val user = User().apply { name = "Alice"; age = 30 }
        val json = user.toJSON()
        assertTrue(json.contains("Alice"))
        assertTrue(json.contains("30"))
    }

    @Test
    fun `toJSONBytes`() {
        val user = User().apply { name = "Alice"; age = 30 }
        val bytes = user.toJSONBytes()
        assertTrue(bytes.isNotEmpty())
        val json = String(bytes)
        assertTrue(json.contains("Alice"))
    }

    @Test
    fun `toJSONPretty`() {
        val user = User().apply { name = "Alice"; age = 30 }
        val json = user.toJSONPretty()
        assertTrue(json.contains("\n"))
    }

    @Test
    fun `null toJSON`() {
        val json: String = null.toJSON()
        assertEquals("null", json)
    }

    // ==================== JSONObject.getAs ====================

    @Test
    fun `JSONObject getAs`() {
        val obj = JSON.parseObject("""{"name":"Alice","age":30}""")
        val name = obj.getAs<String>("name")
        assertEquals("Alice", name)
    }

    @Test
    fun `JSONObject getAs with conversion`() {
        val obj = JSON.parseObject("""{"count":42}""")
        val count = obj.getAs<Int>("count")
        assertEquals(42, count)
    }

    @Test
    fun `JSONObject getAs missing key`() {
        val obj = JSON.parseObject("""{"name":"Alice"}""")
        val missing = obj.getAs<String>("missing")
        assertNull(missing)
    }

    // ==================== JSONArray.toList ====================

    @Test
    fun `JSONArray toList`() {
        val arr = JSON.parseArray(arrayJson)
        val users = arr.toList<User>()
        assertEquals(2, users.size)
        assertEquals("Alice", users[0].name)
    }

    // ==================== Round-trip ====================

    @Test
    fun `round trip`() {
        val original = User().apply { name = "Alice"; age = 30 }
        val json = original.toJSON()
        val restored = json.parseAs<User>()
        assertNotNull(restored)
        assertEquals("Alice", restored.name)
        assertEquals(30, restored.age)
    }
}
