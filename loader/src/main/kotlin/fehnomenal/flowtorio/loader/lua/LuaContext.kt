package fehnomenal.flowtorio.loader.lua

import fehnomenal.flowtorio.loader.buildDefines
import fehnomenal.flowtorio.loader.mod.Mod
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.measureTimeMillis

abstract class LuaContext protected constructor(
    factorioCorePath: Path,
    private val mods: List<Mod>,
    private val tableFactory: LuaTable.Factory,
    globalsInit: (LuaTable) -> Unit
) {
    var eventListener: EventListener = NoopEventListener

    val rawDataTable = tableFactory.newTable()
    private val globals by lazy {
        val g = createGlobals()
        loadFactorioCore(g, factorioCorePath)
        g.also(globalsInit)
    }

    private val modsTable by lazy {
        tableFactory.newTable {
            mods.forEach { mod ->
                it[mod.name] = mod.version.toString()
            }
        }
    }


    fun loadFileForEachMod(fileName: String) {
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
    protected abstract fun createFunctionSerpentBlock(): Any


    protected abstract fun loadFactorioCore(globals: LuaTable, factorioCorePath: Path)


    private fun createGlobals() =
        @Suppress("NestedLambdaShadowedImplicitParameter")
        doCreateGlobals().also {
            it["defines"] = buildDefines(tableFactory)
            it["mods"] = modsTable

            it["log"] = createFunctionLog()

            it["serpent"] = tableFactory.newTable {
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


    interface Factory {
        fun createLuaContext(
            factorioCorePath: Path,
            mods: List<Mod>,
            tableFactory: LuaTable.Factory,
            globalsInit: (LuaTable) -> Unit = {}
        ): LuaContext
    }
}
