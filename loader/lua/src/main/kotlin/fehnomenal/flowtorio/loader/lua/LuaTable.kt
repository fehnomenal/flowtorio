package fehnomenal.flowtorio.loader.lua

interface LuaTable {
    val keys: Iterable<LuaKey>

    operator fun get(key: LuaKey): Any?

    operator fun set(key: LuaKey, value: Any?)
    operator fun set(key: LuaKey, value: Boolean?)
    operator fun set(key: LuaKey, value: Double?)
    operator fun set(key: LuaKey, value: Int?)
    operator fun set(key: LuaKey, value: LuaTable?)
    operator fun set(key: LuaKey, value: String?)

    operator fun get(key: Int) = get(LuaKey.Int(key))
    operator fun get(key: String) = get(LuaKey.String(key))

    operator fun set(key: Int, value: Any?) = set(LuaKey.Int(key), value)
    operator fun set(key: Int, value: Boolean?) = set(LuaKey.Int(key), value)
    operator fun set(key: Int, value: Double?) = set(LuaKey.Int(key), value)
    operator fun set(key: Int, value: Int?) = set(LuaKey.Int(key), value)
    operator fun set(key: Int, value: LuaTable?) = set(LuaKey.Int(key), value)
    operator fun set(key: Int, value: String?) = set(LuaKey.Int(key), value)

    operator fun set(key: String, value: Any?) = set(LuaKey.String(key), value)
    operator fun set(key: String, value: Boolean?) = set(LuaKey.String(key), value)
    operator fun set(key: String, value: Double?) = set(LuaKey.String(key), value)
    operator fun set(key: String, value: Int?) = set(LuaKey.String(key), value)
    operator fun set(key: String, value: LuaTable?) = set(LuaKey.String(key), value)
    operator fun set(key: String, value: String?) = set(LuaKey.String(key), value)

    fun mergeWith(src: LuaTable, keys: Iterable<LuaKey> = src.keys)
}
