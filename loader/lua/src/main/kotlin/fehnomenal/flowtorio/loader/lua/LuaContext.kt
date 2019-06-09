package fehnomenal.flowtorio.loader.lua

import fehnomenal.flowtorio.loader.mod.Mod
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.measureTimeMillis

abstract class LuaContext protected constructor(
    factorioCorePath: Path,
    private val mods: List<Mod>,
    private val luaFactory: LuaFactory,
    globalsInit: (LuaTable) -> Unit,
    private val buildDefines: (LuaFactory) -> LuaTable
) {
    var eventListener: EventListener = NoopEventListener

    val rawDataTable = luaFactory.createLuaTable()
    private val globals by lazy {
        val g = createGlobals()
        loadFactorioCore(g, factorioCorePath)
        g.also(globalsInit)
    }

    private val modsTable by lazy {
        luaFactory.createLuaTable {
            mods.forEach { mod ->
                it[mod.name] = mod.version.toString()
            }
        }
    }


    fun loadFileForEachMod(fileName: String) {
        // Force execution of the lazy handler.
        globals

        mods
            .filter { Files.exists(it.path.resolve(fileName)) }
            .forEach { mod ->
                eventListener.beginLoadingFileForMod(fileName, mod)

                val ms = measureTimeMillis {
                    loadFile(globals, fileName, mod)

                    globals["data"]
                        ?.takeIf { it is LuaTable }
                        ?.let { (it as LuaTable)["raw"] }
                        ?.takeIf { it is LuaTable }
                        ?.let { rawDataTable.mergeWith(it as LuaTable) }
                }

                eventListener.finishLoadingFileForMod(ms)
            }
    }

    protected abstract fun loadFile(globals: LuaTable, fileName: String, mod: Mod)

    protected abstract fun doCreateGlobals(): LuaTable
    protected open fun updateGlobals(globals: LuaTable) = Unit

    protected abstract fun createFunctionLog(): Any
    protected abstract fun createFunctionTableSize(): Any
    protected abstract fun createFunctionSerpentBlock(): Any


    protected abstract fun loadFactorioCore(globals: LuaTable, factorioCorePath: Path)


    private fun createGlobals() =
        @Suppress("NestedLambdaShadowedImplicitParameter")
        doCreateGlobals().also {
            it["defines"] = buildDefines(luaFactory)
            it["mods"] = modsTable

            it["log"] = createFunctionLog()
            it["table_size"] = createFunctionTableSize()

            it["serpent"] = luaFactory.createLuaTable {
                it["block"] = createFunctionSerpentBlock()
            }
        }.also(::updateGlobals)


    interface EventListener {
        fun beginLoadingFileForMod(fileName: String, mod: Mod)
        fun finishLoadingFileForMod(milliSeconds: Long)
    }

    private object NoopEventListener : EventListener {
        override fun beginLoadingFileForMod(fileName: String, mod: Mod) = Unit
        override fun finishLoadingFileForMod(milliSeconds: Long) = Unit
    }
}
