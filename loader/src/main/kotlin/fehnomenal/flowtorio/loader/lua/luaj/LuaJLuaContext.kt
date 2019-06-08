package fehnomenal.flowtorio.loader.lua.luaj

import fehnomenal.flowtorio.loader.lua.LuaContext
import fehnomenal.flowtorio.loader.lua.LuaFactory
import fehnomenal.flowtorio.loader.lua.LuaTable
import fehnomenal.flowtorio.loader.mod.Mod
import org.luaj.vm2.Globals
import org.luaj.vm2.LoadState
import org.luaj.vm2.LuaValue
import org.luaj.vm2.compiler.LuaC
import org.luaj.vm2.lib.Bit32Lib
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.TableLib
import org.luaj.vm2.lib.jse.JseBaseLib
import org.luaj.vm2.lib.jse.JseMathLib
import org.luaj.vm2.lib.jse.JseStringLib
import java.nio.file.Path

class LuaJLuaContext internal constructor(
    factorioCorePath: Path,
    mods: List<Mod>,
    luaFactory: LuaFactory,
    globalsInit: (LuaTable) -> Unit
) :
    LuaContext(factorioCorePath, mods, luaFactory, globalsInit) {

    private val resourceFinder = ExtendedResourceFinder(
        factorioCorePath.resolve("lualib")
    )

    override fun doCreateGlobals() = LuaJLuaTable(Globals().also {
        it.load(JseBaseLib())
        it.load(CustomPackageLib(resourceFinder))
        it.load(Bit32Lib())
        it.load(TableLib())
        it.load(JseStringLib())
        it.load(JseMathLib())
        LoadState.install(it)
        LuaC.install(it)

        it.finder = resourceFinder
    })

    override fun createFunctionLog() = object : OneArgFunction() {
        override fun name() = "log"
        override fun call(arg: LuaValue) = LuaValue.NIL
    }

    override fun createFunctionTableSize() = object : OneArgFunction() {
        override fun name() = "table_size"
        override fun call(arg: LuaValue) = LuaValue.valueOf(arg.checktable().keys().size)
    }

    override fun createFunctionSerpentBlock() = object : OneArgFunction() {
        override fun name() = "serpent.block"
        override fun call(arg: LuaValue) = LuaValue.NIL
    }


    override fun loadFactorioCore(globals: LuaTable, factorioCorePath: Path) {
        globals as LuaJLuaTable
        globals.table as Globals

        globals.table.loadfile("dataloader.lua").call()

        resourceFinder.currentModUri = factorioCorePath.toUri()
        globals.table.loadfile(factorioCorePath.resolve("data.lua").toString()).call()
    }

    override fun loadFile(globals: LuaTable, fileName: String, mod: Mod) {
        globals as LuaJLuaTable
        globals.table as Globals

        resourceFinder.currentModUri = mod.path.toUri()
        globals.table.loadfile(fileName).call()
    }
}
