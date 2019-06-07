package fehnomenal.flowtorio.loader.lua.luaj

import fehnomenal.flowtorio.loader.lua.LuaContext
import fehnomenal.flowtorio.loader.lua.LuaTable
import fehnomenal.flowtorio.loader.mod.Mod
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.jse.JsePlatform
import java.nio.file.Path

class LuaJLuaContext private constructor(
    factorioCorePath: Path,
    mods: List<Mod>,
    tableFactory: LuaTable.Factory,
    globalsInit: (LuaTable) -> Unit
) :
    LuaContext(factorioCorePath, mods, tableFactory, globalsInit) {

    private val resourceFinder = ExtendedResourceFinder(
        factorioCorePath.resolve("lualib")
    )

    override fun doCreateGlobals() = LuaJLuaTable(JsePlatform.standardGlobals().also {
        it.finder = resourceFinder
    })

    override fun createFunctionLog() = object : OneArgFunction() {
        override fun name() = "log"
        override fun call(arg: LuaValue) = LuaValue.NIL
    }

    override fun createFunctionSerpentBlock() = object : OneArgFunction() {
        override fun name() = "serpent.block"
        override fun call(arg: LuaValue) = LuaValue.NIL
    }


    override fun loadFactorioCore(globals: LuaTable, factorioCorePath: Path) {
        globals as LuaJLuaTable

        (globals.table as Globals).loadfile("dataloader.lua").call()

        resourceFinder.currentModUri = factorioCorePath.toUri()
        globals.table.loadfile(factorioCorePath.resolve("data.lua").toString()).call()
    }

    override fun loadFile(globals: LuaTable, fileName: String, mod: Mod) {
        resourceFinder.currentModUri = mod.path.toUri()
        ((globals as LuaJLuaTable).table as Globals).loadfile(fileName).call()
    }


    object Factory : LuaContext.Factory {
        override fun createLuaContext(
            factorioCorePath: Path,
            mods: List<Mod>,
            tableFactory: LuaTable.Factory,
            globalsInit: (LuaTable) -> Unit
        ) = LuaJLuaContext(factorioCorePath, mods, tableFactory, globalsInit)
    }
}
