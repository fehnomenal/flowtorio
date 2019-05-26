package fehnomenal.flowtorio.loader.mod

data class Mod(
    val name: String,
    val title: String,
    val version: Version,
    val dependencies: List<Dependency>
)
