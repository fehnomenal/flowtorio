package fehnomenal.flowtorio.loader.lua

sealed class LuaKey {
    data class Int(val v: kotlin.Int) : LuaKey()
    data class String(val v: kotlin.String) : LuaKey()
}
