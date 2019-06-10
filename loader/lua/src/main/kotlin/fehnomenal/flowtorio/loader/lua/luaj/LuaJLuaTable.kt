package fehnomenal.flowtorio.loader.lua.luaj

import fehnomenal.flowtorio.loader.lua.LuaKey
import fehnomenal.flowtorio.loader.lua.LuaTable
import org.luaj.vm2.*

class LuaJLuaTable internal constructor(internal val table: org.luaj.vm2.LuaTable) : LuaTable {
    override val keys
        get() = table.keys().map {
            when (it) {
                is LuaInteger -> LuaKey.Int(it.toint())
                is LuaString -> LuaKey.String(it.tojstring())
                else -> throw IllegalArgumentException("Unhandled key type ${it.javaClass} of $it")
            }
        }


    private fun LuaKey.toLuaValue() =
        when (this) {
            is LuaKey.Int -> LuaValue.valueOf(v)
            is LuaKey.String -> LuaValue.valueOf(v)
        }

    override fun get(key: LuaKey) = unbox(table[key.toLuaValue()])

    private fun unbox(value: LuaValue?): Any? = when (value) {
        null, is LuaNil -> null
        is LuaBoolean -> value.toboolean()
        is LuaDouble -> value.todouble()
        is LuaInteger -> value.toint()
        is LuaString -> value.tojstring()
        is org.luaj.vm2.LuaTable -> LuaJLuaTable(value)
        else -> throw IllegalArgumentException("Get: unhandled type ${value.javaClass} of $value")
    }


    override fun set(key: LuaKey, value: Any?) {
        value?.let {
            when (it) {
                is Boolean -> set(key, it)
                is Double -> set(key, it)
                is Int -> set(key, it)
                is LuaValue -> table[key.toLuaValue()] = it
                is LuaTable -> set(key, it)
                is String -> set(key, it)
                else -> throw IllegalArgumentException("Set: unhandled type ${it.javaClass} of $it")
            }
        } ?: run { table[key.toLuaValue()] = LuaValue.NIL }
    }

    override fun set(key: LuaKey, value: Boolean?) =
        table.set(key.toLuaValue(), value?.let { LuaValue.valueOf(it) } ?: LuaValue.NIL)

    override fun set(key: LuaKey, value: Double?) =
        table.set(key.toLuaValue(), value?.let { LuaValue.valueOf(it) } ?: LuaValue.NIL)

    override fun set(key: LuaKey, value: Int?) =
        table.set(key.toLuaValue(), value?.let { LuaValue.valueOf(it) } ?: LuaValue.NIL)

    override fun set(key: LuaKey, value: LuaTable?) =
        table.set(key.toLuaValue(), value?.let { (it as LuaJLuaTable).table } ?: LuaValue.NIL)

    override fun set(key: LuaKey, value: String?) =
        table.set(key.toLuaValue(), value?.let { LuaValue.valueOf(it) } ?: LuaValue.NIL)


    override fun mergeWith(src: LuaTable, keys: Iterable<LuaKey>) =
        mergeTables((src as LuaJLuaTable).table, table, keys.map { it.toLuaValue() })

    private fun mergeTables(src: org.luaj.vm2.LuaTable, dst: org.luaj.vm2.LuaTable, keys: Iterable<LuaValue>) {
        keys.forEach {
            val v = src[it]

            if (v.istable()) {
                if (!dst[it].istable()) {
                    dst[it] = org.luaj.vm2.LuaTable()
                }

                mergeTables(v.checktable(), dst[it].checktable(), v.checktable().keys().asList())
            } else if (!v.isnil()) {
                dst[it] = v
            }
        }
    }
}
