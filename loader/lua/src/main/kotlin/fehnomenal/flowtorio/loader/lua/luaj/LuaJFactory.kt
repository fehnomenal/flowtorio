package fehnomenal.flowtorio.loader.lua.luaj

import fehnomenal.flowtorio.loader.lua.LuaFactory
import fehnomenal.flowtorio.loader.lua.LuaTable
import fehnomenal.flowtorio.loader.mod.Mod
import java.nio.file.Path

object LuaJFactory : LuaFactory {
    override fun createLuaTable(init: (LuaTable) -> Unit) = LuaJLuaTable(org.luaj.vm2.LuaTable()).also(init)

    override fun createLuaContext(
        factorioCorePath: Path,
        mods: List<Mod>,
        globalsInit: (LuaTable) -> Unit,
        buildDefines: (LuaFactory) -> LuaTable
    ) = LuaJLuaContext(factorioCorePath, mods, this, globalsInit, buildDefines)
}
