package fehnomenal.flowtorio.loader.mod

import org.json.JSONObject
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.system.measureTimeMillis

object Mods {
    class Available(
        private val factorioBasePath: Path,
        private val factorioModsPath: Path
    ) {
        var eventListener: EventListener = NoopEventListener

        val mods by lazy {
            lateinit var baseMod: Mod
            lateinit var thirdPartyMods: List<Mod>

            val ms = measureTimeMillis {
                baseMod = getModFromPath(factorioBasePath)

                thirdPartyMods = Files.newDirectoryStream(factorioModsPath) { it.fileName.toString().endsWith(".zip") }
                    .mapNotNull {
                        val dirName = it.fileName.toString().removeSuffix(".zip")

                        val fileUri = it.toUri()
                        val zipUri = URI("jar:${fileUri.scheme}", fileUri.path, null)

                        val zipFS = FileSystems.newFileSystem(zipUri, emptyMap<String, Any>())

                        val modPath = zipFS.getPath(dirName)

                        try {
                            getModFromPath(modPath)
                        } catch (e: FileIsNoModException) {
                            null
                        }
                    }
            }

            if (thirdPartyMods.isEmpty()) {
                eventListener.foundBaseGameAndMods(thirdPartyMods.size, ms)
            } else {
                eventListener.foundBaseGame(ms)
            }

            listOf(baseMod) + thirdPartyMods
        }


        private fun getModFromPath(modPath: Path): Mod {
            val info = modPath.resolve("info.json")
            if (!Files.isReadable(info)) {
                throw FileIsNoModException("info.json not found in $modPath")
            }

            return Files.newBufferedReader(info).use {
                val json = JSONObject(it.readText())
                val dependencies = json.optJSONArray("dependencies")?.let { dependencies ->
                    dependencies.toList()
                        .filterIsInstance<String>()
                        .map { dep ->
                            val isOptional = dep.startsWith("?")
                            val name: String
                            val version: Version

                            if (dep.contains(">=")) {
                                val split = dep.removePrefix("?").split(">=")
                                name = split[0].trim()
                                version = Version(split[1].trim())
                            } else {
                                name = dep.removePrefix("?").trim()
                                version = Version("0")
                            }

                            Dependency(name, version, isOptional)
                        }
                }
                    ?: emptyList()

                Mod(
                    json.getString("name"),
                    json.getString("title"),
                    Version(json.getString("version")),
                    dependencies,
                    modPath
                )
            }
        }


        interface EventListener {
            fun foundBaseGame(milliSeconds: Long)
            fun foundBaseGameAndMods(mods: Int, milliSeconds: Long)
        }

        private object NoopEventListener : EventListener {
            override fun foundBaseGame(milliSeconds: Long) = Unit
            override fun foundBaseGameAndMods(mods: Int, milliSeconds: Long) = Unit
        }
    }


    class Loaded(
        private val activeMods: List<Mod>
    ) : Iterable<Mod> {
        var eventListener: EventListener = NoopEventListener

        private val modsInOrder by lazy {
            eventListener.checkedDependencies(measureTimeMillis { checkDependencies() })

            lateinit var modsInOrder: List<Mod>
            eventListener.calculatedModLoadOrder(measureTimeMillis {
                modsInOrder = calculateLoadOrder()
            })

            modsInOrder
        }


        override fun iterator() = modsInOrder.iterator()


        private fun checkDependencies() {
            activeMods.forEach { (name, _, _, dependencies) ->
                dependencies
                    .filterNot { it.isOptional }
                    .forEach { (dependencyName, dependencyVersion, _) ->
                        val dependencyModVersion = activeMods.find { it.name == dependencyName }?.version
                            ?: throw DependencyUnmetException("Mod $dependencyName is required by $name but not available")

                        if (dependencyModVersion < dependencyVersion) {
                            throw DependencyUnmetException("Mod $dependencyName is required in version $dependencyVersion or higher by $name")
                        }
                    }
            }
        }

        private fun calculateLoadOrder(): List<Mod> {
            fun Dependency.withMod(block: (Mod) -> Unit) {
                block(
                    activeMods.find { it.name == mod }
                        ?: if (isOptional) return else error("impossible: all dependencies are met")
                )
            }

            // We need a topological sort over the DAG the dependencies create.

            val dependents = activeMods.associate { it to 0 }.toMutableMap()

            activeMods.forEach { (_, _, _, dependencies) ->
                dependencies.forEach { dependency ->
                    dependency.withMod { dependents[it] = dependents[it]!! + 1 }
                }
            }

            val list = mutableListOf<Mod>()
            val queue = LinkedList<Mod>()
            queue += dependents.filterValues { it == 0 }.keys

            var visitedNodes = 0

            while (queue.isNotEmpty()) {
                visitedNodes++

                val mod = queue.poll()
                list += mod

                mod.dependencies.forEach { dependency ->
                    dependency.withMod {
                        val new = dependents[it]!! - 1
                        dependents[it] = new

                        if (new == 0) {
                            queue.offer(it)
                        }
                    }
                }
            }

            return list.reversed()
        }


        interface EventListener {
            fun checkedDependencies(milliSeconds: Long)
            fun calculatedModLoadOrder(milliSeconds: Long)
        }

        private object NoopEventListener : EventListener {
            override fun checkedDependencies(milliSeconds: Long) = Unit
            override fun calculatedModLoadOrder(milliSeconds: Long) = Unit
        }
    }
}
