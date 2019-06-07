package fehnomenal.flowtorio.loader

import fehnomenal.flowtorio.loader.lua.LuaFactory
import fehnomenal.flowtorio.loader.lua.LuaTable

@Suppress("NestedLambdaShadowedImplicitParameter")
fun buildDefines(luaFactory: LuaFactory): LuaTable {
    // todo: fill with correct values
    return luaFactory.createLuaTable().also {
        it["difficulty_settings"] = luaFactory.createLuaTable().also {
            it["recipe_difficulty"] = luaFactory.createLuaTable().also {
                it["normal"] = 1
            }
            it["technology_difficulty"] = luaFactory.createLuaTable().also {
                it["normal"] = 1
            }
        }

        it["direction"] = luaFactory.createLuaTable().also {
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
