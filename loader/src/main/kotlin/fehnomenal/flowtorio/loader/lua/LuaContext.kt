package fehnomenal.flowtorio.loader.lua

import fehnomenal.flowtorio.loader.Defines
import fehnomenal.flowtorio.loader.mod.Mod
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.JsePlatform
import java.nio.file.Path

class LuaContext(
    private val factorioCorePath: Path,
    private val mods: List<Mod>
) {
    private val modsTable by lazy {
        LuaTable().also {
            mods.forEach { mod ->
                it[mod.name] = mod.version.toString()
            }
        }
    }

    private val resourceFinder = ExtendedResourceFinder(
        factorioCorePath.resolve("lualib")
    )


    private fun initializeGlobals() =
        JsePlatform.standardGlobals().also {
            it.finder = resourceFinder

            it["defines"] = Defines

            it["mods"] = modsTable
        }

    private fun Globals.loadFactorioCore() {
        loadfile("dataloader.lua").call()

        resourceFinder.currentModUri = factorioCorePath.toUri()
        loadfile(factorioCorePath.resolve("data.lua").toString()).call()
    }


    companion object {
        fun mergeLuaTables(
            src: LuaTable,
            dst: LuaTable,
            keys: Iterable<LuaValue> = src.keys().asIterable()
        ) {
            keys.forEach { k ->
                val v = src[k]

                if (v.istable()) {
                    if (!dst[k].istable()) {
                        dst[k] = LuaTable()
                    }
                    mergeLuaTables(v.checktable(), dst[k].checktable())
                } else if (!v.isnil()) {
                    dst[k] = v
                }
            }
        }
    }
}
