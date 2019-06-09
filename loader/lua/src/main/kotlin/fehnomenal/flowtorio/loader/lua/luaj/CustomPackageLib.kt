package fehnomenal.flowtorio.loader.lua.luaj

import org.luaj.vm2.Globals
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.PackageLib
import java.io.File
import java.nio.file.Path

class CustomPackageLib(private val resourceFinder: ExtendedResourceFinder) : PackageLib() {
    companion object {
        private val SENTINEL = LuaValue.valueOf("\u0001")
    }


    private lateinit var globals: Globals
    private lateinit var loadedTable: LuaTable


    override fun call(modname: LuaValue, env: LuaValue): LuaTable =
        super.call(modname, env).checkglobals().also {
            it["require"] = Require()

            loadedTable = it["package"]["loaded"].checktable()

            globals = it
        }

    inner class Require : require() {
        override fun call(arg: LuaValue): LuaValue {
            val name = arg.checkstring()

            var result = loadedTable[name]

            if (result.toboolean()) {
                if (result === SENTINEL) {
                    LuaValue.error("loop or previous error loading module '$name'")
                }
                return result
            }

            val fileName = name.checkjstring().replace('.', File.separatorChar) + ".lua"
            val path = resourceFinder.findPath(fileName)
                ?: return LuaValue.error("module '$name' not found: $fileName")

            val fileBelongsToMod = path.startsWith(Path.of(resourceFinder.currentModUri))


            val v = globals.loadfile(fileName)
            if (v.arg1().isfunction()) {
                loadedTable[name] = SENTINEL

                result = v.arg1().call(name, LuaValue.valueOf(fileName))
                if (!result.isnil()) {
                    if (fileBelongsToMod) {
                        loadedTable[name] = LuaValue.NIL
                    } else {
                        loadedTable[name] = result
                    }
                } else {
                    result = loadedTable[name]
                    if (result === SENTINEL) {
                        result = LuaValue.TRUE

                        if (fileBelongsToMod) {
                            loadedTable[name] = LuaValue.NIL
                        } else {
                            loadedTable[name] = result
                        }
                    }
                }

                return result
            } else {
                // report error
                return LuaValue.error("'" + fileName + "': " + v.arg(2).tojstring())
            }
        }
    }
}
