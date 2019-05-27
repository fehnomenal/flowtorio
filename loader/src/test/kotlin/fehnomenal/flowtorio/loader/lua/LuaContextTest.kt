package fehnomenal.flowtorio.loader.lua

import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue

class LuaContextTest : StringSpec({
    "merge empty lua tables" {
        val dst = LuaTable()
        LuaContext.mergeLuaTables(LuaTable(), dst)
        dst.keys().size shouldBe 0
    }

    "merge lua tables with additional value" {
        val dst = LuaTable().also { it["abc"] = 13 }
        LuaContext.mergeLuaTables(LuaTable().also { it["def"] = 37 }, dst)
        dst.keys().map { it.toString() } shouldContainExactly listOf("abc", "def")
        dst["abc"].checkint() shouldBe 13
        dst["def"].checkint() shouldBe 37
    }

    "merge lua tables with additional nil value" {
        val dst = LuaTable().also { it["abc"] = 13 }
        LuaContext.mergeLuaTables(LuaTable().also { it["def"] = LuaValue.NIL }, dst)
        dst.keys().map { it.toString() } shouldContainExactly listOf("abc")
        dst["abc"].checkint() shouldBe 13
    }

    "merge lua tables with value override" {
        val dst = LuaTable().also { it["abc"] = 13 }
        LuaContext.mergeLuaTables(LuaTable().also { it["abc"] = 37 }, dst)
        dst.keys().map { it.toString() } shouldContainExactly listOf("abc")
        dst["abc"].checkint() shouldBe 37
    }

    "merge lua tables with nil value override" {
        val dst = LuaTable().also { it["abc"] = 13 }
        LuaContext.mergeLuaTables(LuaTable().also { it["abc"] = LuaValue.NIL }, dst)
        dst.keys().map { it.toString() } shouldContainExactly listOf("abc")
        dst["abc"].checkint() shouldBe 13
    }

    "merge nested lua tables" {
        val dst = LuaTable()
        val src = LuaTable().also {
            it["abc"] = LuaTable().also { it["def"] = 13 }
        }
        LuaContext.mergeLuaTables(src, dst)
        dst.keys().map { it.toString() } shouldContainExactly listOf("abc")
        dst["abc"].checktable().keys().map { it.toString() } shouldContainExactly listOf("def")
        dst["abc"].checktable()["def"].checkint() shouldBe 13
    }

    "merge nested lua tables with reference change" {
        val dst = LuaTable()
        val src = LuaTable().also {
            it["abc"] = LuaTable().also { it["def"] = 13 }
        }
        LuaContext.mergeLuaTables(src, dst)
        dst.keys().map { it.toString() } shouldContainExactly listOf("abc")
        dst["abc"].checktable().keys().map { it.toString() } shouldContainExactly listOf("def")
        dst["abc"].checktable()["def"].checkint() shouldBe 13

        src["abc"].checktable()["def"] = 37

        dst["abc"].checktable()["def"].checkint() shouldBe 13
    }
})
