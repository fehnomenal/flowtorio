package fehnomenal.flowtorio.loader.lua.luaj

import org.luaj.vm2.lib.ResourceFinder
import java.io.InputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path

class ExtendedResourceFinder(
    private val coreLuaLibDir: Path
) : ResourceFinder {
    var currentModUri: URI? = null

    private val lastPaths = LinkedHashSet<Path>()

    override fun findResource(fileName: String): InputStream? {
        val path = findPath(fileName)
        if (path != null) {
            lastPaths.add(path)

            return Files.newInputStream(path)
        }


        if (fileName.startsWith("__")) {
            TODO("Resolve $fileName from another mod")
        }

        return null
    }

    internal fun findPath(fileName: String): Path? {
        Path.of(fileName).takeIf { Files.exists(it) }?.let { return it }

        currentModUri?.let { uri ->
            Path.of(uri).resolve(fileName).takeIf { Files.exists(it) }?.let { return it }
        }

        lastPaths.find { it.endsWith(fileName) }?.let { return it }
        lastPaths.asSequence()
            .map { it.resolveSibling(fileName) }
            .firstOrNull { Files.exists(it) }
            ?.let { return it }

        coreLuaLibDir.resolve(fileName).takeIf { Files.exists(it) }?.let { return it }

        return null
    }
}
