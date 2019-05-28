package fehnomenal.flowtorio.loader

import org.luaj.vm2.LuaTable

// todo: fill with correct values
@Suppress("NestedLambdaShadowedImplicitParameter")
object Defines : LuaTable() {
    init {
        this["difficulty_settings"] = LuaTable().also {
            it["recipe_difficulty"] = LuaTable().also {
                it["normal"] = 1
            }
            it["technology_difficulty"] = LuaTable().also {
                it["normal"] = 1
            }
        }

        this["direction"] = LuaTable().also {
            it["north"] = 1
            it["northeast"] = 1
            it["east"] = 1
            it["southeast"] = 1
            it["south"] = 1
            it["southwest"] = 1
            it["west"] = 1
            it["northwest"] = 1
        }

    }
}
