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
            TODO()
        }
    }
}
