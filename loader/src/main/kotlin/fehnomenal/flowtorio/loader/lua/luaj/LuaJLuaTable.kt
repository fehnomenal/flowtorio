package fehnomenal.flowtorio.loader.lua.luaj

import fehnomenal.flowtorio.loader.lua.LuaTable
import org.luaj.vm2.LuaValue

class LuaJLuaTable(private val table: org.luaj.vm2.LuaTable, override val factory: LuaTable.Factory) : LuaTable {
    override val keys
        get() = table.keys().map { it.checkjstring() }


    override fun get(key: Int) = unbox(table[key])

    override fun get(key: String) = unbox(table[key])

    private fun unbox(value: LuaValue?): Any? = when {
        value == null || value.isnil() -> null
        value.istable() -> LuaJLuaTable(value as org.luaj.vm2.LuaTable, factory)
        value.isint() -> value.checkint()
        value.islong() -> value.checklong()
        value.isstring() -> value.checkjstring()
        value.isboolean() -> value.checkboolean()
        else -> throw IllegalArgumentException("Unhandled type ${value.javaClass} of $value")
    }


    override fun set(key: Int, value: Any?) {
        value?.let {
            when (it) {
                is Boolean -> set(key, it)
                is Double -> set(key, it)
                is Int -> set(key, it)
                is LuaTable -> set(key, it)
                is String -> set(key, it)
                is LuaValue -> table[key] = it
                else -> throw IllegalArgumentException("Unhandled type ${it.javaClass} of $it")
            }
        } ?: run { table[key] = LuaValue.NIL }
    }

    override fun set(key: Int, value: Boolean?) {
        value?.let { table[key] = LuaValue.valueOf(it) } ?: run { table[key] = LuaValue.NIL }
    }

    override fun set(key: Int, value: Double?) {
        value?.let { table[key] = LuaValue.valueOf(it) } ?: run { table[key] = LuaValue.NIL }
    }

    override fun set(key: Int, value: Int?) {
        value?.let { table[key] = LuaValue.valueOf(it) } ?: run { table[key] = LuaValue.NIL }
    }

    override fun set(key: Int, value: LuaTable?) {
        value?.let {
            table[key] = (it as LuaJLuaTable).table
        } ?: run { table[key] = LuaValue.NIL }
    }

    override fun set(key: Int, value: String?) {
        value?.let { table[key] = value } ?: run { table[key] = LuaValue.NIL }
    }


    override fun set(key: String, value: Any?) {
        value?.let {
            when (it) {
                is Boolean -> set(key, it)
                is Double -> set(key, it)
                is Int -> set(key, it)
                is LuaTable -> set(key, it)
                is String -> set(key, it)
                is LuaValue -> table[key] = it
                else -> throw IllegalArgumentException("Unhandled type ${it.javaClass} of $it")
            }
        } ?: run { table[key] = LuaValue.NIL }
    }

    override fun set(key: String, value: Boolean?) {
        value?.let { table[key] = LuaValue.valueOf(it) } ?: run { table[key] = LuaValue.NIL }
    }

    override fun set(key: String, value: Double?) {
        value?.let { table[key] = it } ?: run { table[key] = LuaValue.NIL }
    }

    override fun set(key: String, value: Int?) {
        value?.let { table[key] = it } ?: run { table[key] = LuaValue.NIL }
    }

    override fun set(key: String, value: LuaTable?) {
        value?.let {
            table[key] = (it as LuaJLuaTable).table
        } ?: run { table[key] = LuaValue.NIL }
    }

    override fun set(key: String, value: String?) {
        value?.let { table[key] = value } ?: run { table[key] = LuaValue.NIL }
    }


    object Factory : LuaTable.Factory {
        override fun newTable(init: (LuaTable) -> Unit) = LuaJLuaTable(org.luaj.vm2.LuaTable(), this).also(init)
    }
}
