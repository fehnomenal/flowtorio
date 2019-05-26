package fehnomenal.flowtorio.loader.mod

data class Mod(
    val name: String,
    val version: Version,
    val dependencies: List<Dependency>
)
