package fehnomenal.flowtorio.loader.lua

import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue

class LuaContext {
    companion object {
        fun mergeLuaTables(
            src: LuaTable,
            dst: LuaTable,
            keys: Iterable<LuaValue> = src.keys().asIterable()
        ) {
            keys.forEach { k ->
                val v = src[k]

                if (v.istable()) {
                    if (!dst[k].istable()) {
                        dst[k] = LuaTable()
                    }
                    mergeLuaTables(v.checktable(), dst[k].checktable())
                } else if (!v.isnil()) {
                    dst[k] = v
                }
            }
        }
    }
}
