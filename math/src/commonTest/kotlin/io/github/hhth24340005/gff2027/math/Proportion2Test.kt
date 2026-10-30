package io.github.hhth24340005.gff2027.math

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@Suppress("unused")
class Proportion2Test :
  FunSpec({
    // Above 0.5, so the stored bits have the sign bit set
    val high = Proportion.fromRateOrThrow(0.75)
    val low = Proportion.fromRateOrThrow(0.25)

    test("components come back out") {
      val point = Proportion2(high.fromLeft, low.fromBottom)
      point.horizontal shouldBe high.fromLeft
      point.vertical shouldBe low.fromBottom
    }

    test("the far edges measure the rest") {
      val point = Proportion2(low.fromRight, high.fromTop)
      point.horizontal.fromLeft shouldBe high
      point.horizontal.fromRight shouldBe low
      point.vertical.fromBottom shouldBe low
      point.vertical.fromTop shouldBe high
    }

    test("both ends come back out") {
      val corner =
        Proportion2(Proportion.zero.fromRight, Proportion.zero.fromTop)
      corner.horizontal.fromLeft shouldBe Proportion.one
      corner.vertical.fromBottom shouldBe Proportion.one
      val opposite =
        Proportion2(Proportion.zero.fromLeft, Proportion.zero.fromBottom)
      opposite.horizontal.fromLeft shouldBe Proportion.zero
      opposite.vertical.fromBottom shouldBe Proportion.zero
    }

    test("equal components make equal points") {
      Proportion2(high.fromLeft, low.fromBottom) shouldBe
        Proportion2(high.fromLeft, low.fromBottom)
      Proportion2(high.fromLeft, low.fromBottom) shouldNotBe
        Proportion2(low.fromLeft, high.fromBottom)
    }

    test("toString shows both components") {
      val point = Proportion2(low.fromLeft, high.fromBottom)
      point.toString() shouldBe "(→ 25.0%, ↑ 75.0%)"
      point.toString(precision = 0) shouldBe "(→ 25%, ↑ 75%)"
    }
  })
