package fehnomenal.flowtorio.loader.lua

interface LuaTable {
    val factory: Factory
    val keys: Iterable<String>

    operator fun get(key: Int): Any?
    operator fun get(key: String): Any?

    operator fun set(key: Int, value: Any?)
    operator fun set(key: Int, value: Boolean?)
    operator fun set(key: Int, value: Double?)
    operator fun set(key: Int, value: Int?)
    operator fun set(key: Int, value: String?)
    operator fun set(key: Int, value: LuaTable?)

    operator fun set(key: String, value: Any?)
    operator fun set(key: String, value: Boolean?)
    operator fun set(key: String, value: Double?)
    operator fun set(key: String, value: Int?)
    operator fun set(key: String, value: String?)
    operator fun set(key: String, value: LuaTable?)

    fun mergeWith(
        src: LuaTable,
        keys: Iterable<String> = src.keys
    ) {
        keys.forEach { k ->
            val v = src[k]

            if (v is LuaTable) {
                if (this[k] !is LuaTable) {
                    this[k] = v.factory.newTable()
                }
                (this[k] as LuaTable).mergeWith(v)
            } else if (v != null) {
                this[k] = v
            }
        }
    }


    interface Factory {
        fun newTable(init: (LuaTable) -> Unit = {}): LuaTable
    }
}
