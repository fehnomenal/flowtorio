package fehnomenal.flowtorio.loader

import fehnomenal.flowtorio.common.Difficulty
import fehnomenal.flowtorio.common.Fluid
import fehnomenal.flowtorio.common.Item
import fehnomenal.flowtorio.common.Recipe
import fehnomenal.flowtorio.loader.lua.LuaContext
import fehnomenal.flowtorio.loader.lua.LuaFactory
import fehnomenal.flowtorio.loader.lua.LuaKey
import fehnomenal.flowtorio.loader.lua.LuaTable
import fehnomenal.flowtorio.loader.lua.luaj.LuaJFactory
import fehnomenal.flowtorio.loader.mod.Mods
import java.nio.file.Path

private val luaFactory: LuaFactory = LuaJFactory

data class Data(val items: List<Item>, val fluids: List<Fluid>, val recipes: List<Recipe>)

fun loadFactorioData(
    factorioCorePath: Path,
    modsToLoad: Mods.ToLoad,
    difficulty: Difficulty,
    eventListener: LuaContext.EventListener
): Data {
    val settingsTable = loadFiles(factorioCorePath, modsToLoad, eventListener, "settings")
    val settings = translateSettings(settingsTable)

    val dataTable = loadFiles(factorioCorePath, modsToLoad, eventListener, "data") {
        it["settings"] = settings
    }


    val items = collectAllItems(dataTable)
    val fluids = collectAllFluids(dataTable)
    val recipes = translateRecipePrototypes(
        dataTable["recipe"] as LuaTable,
        difficulty,
        items.associateBy { it.name },
        fluids.associateBy { it.name }
    )

    return Data(items, fluids, recipes)
}


private fun loadFiles(
    factorioCorePath: Path,
    modsToLoad: Mods.ToLoad,
    eventListener: LuaContext.EventListener,
    filePrefix: String,
    globalsInit: (LuaTable) -> Unit = {}
): LuaTable {
    val context = luaFactory.createLuaContext(factorioCorePath, modsToLoad.toList(), globalsInit, ::buildDefines)
    context.eventListener = eventListener

    listOf(
        "$filePrefix.lua",
        "$filePrefix-updates.lua",
        "$filePrefix-final-fixes.lua"
    ).forEach { context.loadFileForEachMod(it) }

    return context.rawDataTable
}

private fun translateSettings(settingsTable: LuaTable): LuaTable {
    val startupSettings = luaFactory.createLuaTable()
    val keys = arrayOf(
        LuaKey.String("bool-setting"),
        LuaKey.String("double-setting"),
        LuaKey.String("int-setting"),
        LuaKey.String("string-setting")
    )

    keys
        .filter { it in settingsTable.keys }
        .forEach { key ->
            val settings = settingsTable[key] as LuaTable
            val settingNames = settings.keys

            settingNames.forEach { settingName ->
                val setting = settings[settingName] as LuaTable

                if (setting["setting_type"] == "startup") {
                    startupSettings[setting["name"] as String] = luaFactory.createLuaTable {
                        it["value"] = setting["default_value"]
                    }
                }
            }
        }

    return luaFactory.createLuaTable {
        it["startup"] = startupSettings
    }
}

private fun collectAllItems(dataTable: LuaTable): List<Item> {
    val itemsTable = dataTable["item"] as LuaTable
    listOf(
        // All subtypes of 'item'.
        "ammo",
        "capsule",
        "gun",
        "item-with-entity-data",
        "item-with-label",
        "item-with-inventory",
        "blueprint-book",
        "item-with-tags",
        "selection-tool",
        "blueprint",
        "copy-paste-tool",
        "deconstruction-item",
        "upgrade-item",
        "module",
        "rail-planner",
        "tool",
        "armor",
        "mining-tool",
        "repair-tool"
    )
        .mapNotNull { dataTable[it] as? LuaTable }
        .forEach { itemsTable.mergeWith(it) }

    return translateItemPrototypes(itemsTable)
}

private fun collectAllFluids(dataTable: LuaTable): List<Fluid> {
    val fluidsTable = dataTable["fluid"] as LuaTable
    listOf(
        // Currently there are no subtypes of 'fluid'.
        ""
    )
        .mapNotNull { dataTable[it] as? LuaTable }
        .forEach { fluidsTable.mergeWith(it) }

    return translateFluidPrototypes(fluidsTable)
}
