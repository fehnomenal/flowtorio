package fehnomenal.flowtorio.loader.mod

import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainInOrder
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldEndWith
import io.kotlintest.shouldNotThrow
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import java.nio.file.Path

class ModsTest : StringSpec({
    "missing dependency" {
        shouldThrow<DependencyUnmetException> {
            Mods.Loaded(
                listOf(
                    Mod(
                        "dependent", "My Dependent Mod", Version("1.0.0"), listOf(
                            Dependency("dependency", Version("0.5"), false)
                        ),
                        Path.of("")
                    )
                )
            ).iterator()
        }.message shouldEndWith "but not available"

        shouldNotThrow<DependencyUnmetException> {
            Mods.Loaded(
                listOf(
                    Mod(
                        "dependent", "My Dependent Mod", Version("1.0.0"), listOf(
                            Dependency("dependency", Version("0.5"), true)
                        ),
                        Path.of("")
                    )
                )
            ).iterator()
        }
    }

    "wrong dependency version" {
        shouldThrow<DependencyUnmetException> {
            Mods.Loaded(
                listOf(
                    Mod(
                        "dependent", "My Dependent Mod", Version("1.0.0"), listOf(
                            Dependency("dependency", Version("0.5"), false)
                        ),
                        Path.of("")
                    ),
                    Mod(
                        "dependency", "The dependency", Version("0.3"), emptyList(),
                        Path.of("")
                    )
                )
            ).iterator()
        }.message shouldContain "or higher"

        shouldNotThrow<DependencyUnmetException> {
            Mods.Loaded(
                listOf(
                    Mod(
                        "dependent", "My Dependent Mod", Version("1.0.0"), listOf(
                            Dependency("dependency", Version("0.5"), false)
                        ),
                        Path.of("")
                    ),
                    Mod(
                        "dependency", "The dependency", Version("0.5"), emptyList(),
                        Path.of("")
                    )
                )
            ).iterator()
        }
    }

    "single mod" {
        val mod = Mod("mod", "Mod", Version("1.0.0"), emptyList(), Path.of(""))

        Mods.Loaded(listOf(mod)).toList() shouldContainExactly listOf(mod)
    }

    "single dependency" {
        val dependency = Mod("dep", "Dependency", Version("1.4.0"), emptyList(), Path.of(""))
        val mod = Mod(
            "mod",
            "Mod",
            Version("1.0.0"),
            listOf(
                Dependency("dep", Version("1"), false)
            ),
            Path.of("")
        )

        Mods.Loaded(listOf(mod, dependency)).toList() shouldContainExactly listOf(dependency, mod)
    }

    "two dependents" {
        val dependency = Mod("dep", "Dependency", Version("1.4.0"), emptyList(), Path.of(""))
        val mod1 = Mod(
            "mod1",
            "Mod 1",
            Version("1.0.0"),
            listOf(
                Dependency("dep", Version("1"), false)
            ),
            Path.of("")
        )
        val mod2 = Mod(
            "mod2",
            "Mod 2",
            Version("0.3"),
            listOf(
                Dependency("dep", Version("0"), false)
            ),
            Path.of("")
        )

        val modsToLoad = Mods.Loaded(listOf(mod1, dependency, mod2)).toList()
        modsToLoad shouldContainInOrder listOf(dependency, mod1)
        modsToLoad shouldContainInOrder listOf(dependency, mod2)
    }
})
