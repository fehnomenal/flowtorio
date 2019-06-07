package fehnomenal.flowtorio.loader.lua

import fehnomenal.flowtorio.loader.mod.Mod
import java.nio.file.Path

interface LuaFactory {
    fun createLuaTable(init: (LuaTable) -> Unit = {}): LuaTable

    fun createLuaContext(
        factorioCorePath: Path,
        mods: List<Mod>,
        globalsInit: (LuaTable) -> Unit = {}
    ): LuaContext
}
