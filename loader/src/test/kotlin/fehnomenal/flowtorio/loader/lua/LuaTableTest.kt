package fehnomenal.flowtorio.loader.lua

import io.kotlintest.matchers.beEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.luaj.vm2.LuaValue

abstract class LuaTableTest(tableFactory: LuaTable.Factory) : StringSpec({
    "merge empty lua tables" {
        val dst = tableFactory.newTable()
        dst.mergeWith(tableFactory.newTable())
        dst.keys.toList() should beEmpty()
    }

    "merge lua tables with additional value" {
        val dst = tableFactory.newTable { it["abc"] = 13 }
        dst.mergeWith(tableFactory.newTable { it["def"] = 37 })
        dst.keys.toList() shouldContainExactly listOf("abc", "def")
        dst["abc"] shouldBe 13
        dst["def"] shouldBe 37
    }

    "merge lua tables with additional nil value" {
        val dst = tableFactory.newTable { it["abc"] = 13 }
        dst.mergeWith(tableFactory.newTable { it["def"] = LuaValue.NIL })
        dst.keys.toList() shouldContainExactly listOf("abc")
        dst["abc"] shouldBe 13
    }

    "merge lua tables with value override" {
        val dst = tableFactory.newTable { it["abc"] = 13 }
        dst.mergeWith(tableFactory.newTable { it["abc"] = 37 })
        dst.keys.toList() shouldContainExactly listOf("abc")
        dst["abc"] shouldBe 37
    }

    "merge lua tables with nil value override" {
        val dst = tableFactory.newTable { it["abc"] = 13 }
        dst.mergeWith(tableFactory.newTable { it["abc"] = LuaValue.NIL })
        dst.keys.toList() shouldContainExactly listOf("abc")
        dst["abc"] shouldBe 13
    }

    "merge nested lua tables" {
        val dst = tableFactory.newTable()
        val src = tableFactory.newTable {
            it["abc"] = tableFactory.newTable { it["def"] = 13 }
        }
        dst.mergeWith(src)
        dst.keys.toList() shouldContainExactly listOf("abc")
        (dst["abc"] as LuaTable).keys.toList() shouldContainExactly listOf("def")
        (dst["abc"] as LuaTable)["def"] shouldBe 13
    }

    "merge nested lua tables with reference change" {
        val dst = tableFactory.newTable()
        val src = tableFactory.newTable {
            it["abc"] = tableFactory.newTable { it["def"] = 13 }
        }
        dst.mergeWith(src)
        dst.keys.toList() shouldContainExactly listOf("abc")
        (dst["abc"] as LuaTable).keys.toList() shouldContainExactly listOf("def")
        (dst["abc"] as LuaTable)["def"] shouldBe 13

        (src["abc"] as LuaTable)["def"] = 37

        (dst["abc"] as LuaTable)["def"] shouldBe 13
    }
})
