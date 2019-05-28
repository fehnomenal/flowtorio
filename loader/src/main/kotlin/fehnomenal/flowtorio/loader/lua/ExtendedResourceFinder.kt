package fehnomenal.flowtorio.loader.lua

import org.luaj.vm2.lib.ResourceFinder
import java.io.InputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ExtendedResourceFinder(
    private val coreLuaLibDir: Path
) : ResourceFinder {
    var currentModUri: URI? = null

    private var lastPath: Path? = null

    override fun findResource(fileName: String): InputStream? {
        val path = findPath(fileName)
        if (path != null) {
            lastPath = path

            return Files.newInputStream(path)
        }


        if (fileName.startsWith("__")) {
            TODO("Resolve $fileName from another mod")
        }

        return null
    }

    private fun findPath(fileName: String): Path? {
        Paths.get(fileName).takeIf { Files.exists(it) }?.let { return it }

        currentModUri?.let { uri ->
            Paths.get(uri).resolve(fileName).takeIf { Files.exists(it) }?.let { return it }
        }

        lastPath?.takeIf { it.endsWith(fileName) }?.let { return it }
        lastPath?.resolveSibling(fileName)?.takeIf { Files.exists(it) }?.let { return it }

        coreLuaLibDir.resolve(fileName).takeIf { Files.exists(it) }?.let { return it }

        return null
    }
}
