package fehnomenal.flowtorio.loader.mod

import java.nio.file.Path

data class Mod(
    val name: String,
    val title: String,
    val version: Version,
    val dependencies: List<Dependency>,
    val path: Path
)
