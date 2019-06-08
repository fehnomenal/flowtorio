package fehnomenal.flowtorio.loader.mod

data class Version(private val v: String) : Comparable<Version> {
    companion object {
        private val regex = "[0-9]+(\\.[0-9]+)*".toRegex()
    }


    private val parts by lazy { v.split('.') }


    init {
        if (!v.matches(regex)) {
            throw IllegalArgumentException("Version string '$v' is invalid.")
        }
    }

    override fun compareTo(other: Version): Int {
        val length = Math.max(parts.size, other.parts.size)
        for (i in 0 until length) {
            val thisPart = parts.getOrNull(i)?.toInt() ?: 0
            val thatPart = other.parts.getOrNull(i)?.toInt() ?: 0

            if (thisPart < thatPart) {
                return -1
            }
            if (thisPart > thatPart) {
                return 1
            }
        }
        return 0
    }

    override fun toString() = v
}
