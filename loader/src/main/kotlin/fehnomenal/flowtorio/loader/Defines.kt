package fehnomenal.flowtorio.loader

import fehnomenal.flowtorio.loader.lua.LuaTable

@Suppress("NestedLambdaShadowedImplicitParameter")
fun buildDefines(tableFactory: LuaTable.Factory): LuaTable {
    // todo: fill with correct values
    return tableFactory.newTable().also {
        it["difficulty_settings"] = tableFactory.newTable().also {
            it["recipe_difficulty"] = tableFactory.newTable().also {
                it["normal"] = 1
            }
            it["technology_difficulty"] = tableFactory.newTable().also {
                it["normal"] = 1
            }
        }

        it["direction"] = tableFactory.newTable().also {
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
