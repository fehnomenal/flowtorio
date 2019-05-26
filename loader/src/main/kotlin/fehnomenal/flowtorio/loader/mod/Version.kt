package fehnomenal.flowtorio.loader.mod

data class Version(val v: String) : Comparable<Version> {
    companion object {
        private val regex = "[0-9]+(\\.[0-9]+)*".toRegex()
    }


    init {
        if (!v.matches(regex)) {
            throw IllegalArgumentException("Version string '$v' is invalid.")
        }
    }

    override fun compareTo(other: Version): Int {
        TODO()
    }
}
