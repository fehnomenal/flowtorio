package fehnomenal.flowtorio.loader.mod

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec

class VersionTest : StringSpec({
    "illegal version string" {
        shouldThrow<IllegalArgumentException> { Version("") }
        shouldThrow<IllegalArgumentException> { Version("alpha") }
        shouldThrow<IllegalArgumentException> { Version("0.1.0-alpha") }
    }

    "comparison" {
        (Version("0") < Version("0")) shouldBe false
        (Version("0") <= Version("0")) shouldBe true
        (Version("0") < Version("1")) shouldBe true
        (Version("0") < Version("0.1")) shouldBe true
        (Version("0.1") < Version("0")) shouldBe false
        (Version("0.1") < Version("1")) shouldBe true
    }
})
