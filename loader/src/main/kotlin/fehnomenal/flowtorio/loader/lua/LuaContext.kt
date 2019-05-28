package fehnomenal.flowtorio.loader.lua

import fehnomenal.flowtorio.loader.Defines
import fehnomenal.flowtorio.loader.mod.Mod
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.JsePlatform
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.measureTimeMillis

class LuaContext(
    private val factorioCorePath: Path,
    private val mods: List<Mod>
) {
    var eventListener: EventListener = NoopEventListener

    val globalData = LuaTable()

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


    fun loadFileForEachMod(fileName: String) {
        mods
            .filter { Files.exists(it.path.resolve(fileName)) }
            .forEach {
                eventListener.beginLoadingFileForMod(fileName, it)
                val ms = measureTimeMillis { loadFile(fileName, it) }
                eventListener.finishLoadingFileForMod(ms)
            }
    }

    private fun loadFile(fileName: String, mod: Mod) {
        val globals = initializeGlobals()
        val originalKeys = globals.keys().asList()

        globals.loadFactorioCore()

        mergeLuaTables(globalData, globals)

        resourceFinder.currentModUri = mod.path.toUri()
        globals.loadfile(fileName).call()

        mergeLuaTables(globals, globalData, globals.keys() subtract originalKeys)
    }

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


    interface EventListener {
        fun beginLoadingFileForMod(fileName: String, mod: Mod)
        fun finishLoadingFileForMod(milliSeconds: Long)
    }

    private object NoopEventListener : EventListener {
        override fun beginLoadingFileForMod(fileName: String, mod: Mod) = Unit
        override fun finishLoadingFileForMod(milliSeconds: Long) = Unit
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
