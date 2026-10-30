package io.github.hhth24340005.gff2027.math

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Suppress("unused")
class LengthTest :
  FunSpec({
    test("constructors reject a negative distance") {
      shouldThrow<IllegalArgumentException> { (-1).meters }
      shouldThrow<IllegalArgumentException> { (-0.001).millimeters }
      shouldThrow<IllegalArgumentException> { (-1.0f).centimeters }
      shouldThrow<IllegalArgumentException> { (-1L).kilometers }
    }

    test("constructors accept -0.0 as 0") {
      (-0.0).meters shouldBe Distance.zero
    }

    test("coerceAtMost and coerceAtLeast pick by value") {
      3.meters.coerceAtMost(2.meters) shouldBe 2.meters
      2.meters.coerceAtMost(3.meters) shouldBe 2.meters
      3.meters.coerceAtLeast(2.meters) shouldBe 3.meters
      2.meters.coerceAtLeast(3.meters) shouldBe 3.meters
    }

    test("coerceIn clamps into the range") {
      5.meters.coerceIn(1.meters, 2.meters) shouldBe 2.meters
      0.meters.coerceIn(1.meters, 2.meters) shouldBe 1.meters
      1.5.meters.coerceIn(1.meters, 2.meters) shouldBe 1.5.meters
      5.meters.coerceIn(1.meters, 1.meters) shouldBe 1.meters
    }

    test("coerceIn rejects an empty range") {
      shouldThrow<IllegalArgumentException> {
        0.meters.coerceIn(2.meters, 1.meters)
      }
    }

    test("toString shows meters") {
      1.5.meters.toString() shouldBe "1.500 m"
      1.5.meters.toString(precision = 1) shouldBe "1.5 m"
    }

    test("left and right are opposite") {
      3.meters.left shouldBe -(3.meters.right)
      3.meters.down shouldBe -(3.meters.up)
    }

    test("zero is the same toward either side") {
      0.meters.left shouldBe HorizontalDistance.zero
      0.meters.right shouldBe HorizontalDistance.zero
      0.meters.down shouldBe VerticalDistance.zero
      0.meters.up shouldBe VerticalDistance.zero
    }

    test("plus and minus add along the axis") {
      3.meters.right + 5.meters.left shouldBe 2.meters.left
      3.meters.up - 5.meters.up shouldBe 2.meters.down
    }

    test("plus rejects a sum beyond the Float range") {
      val far = Float.MAX_VALUE.meters.right
      shouldThrow<ArithmeticException> { far + far }
    }

    test("times scales along the axis") {
      3.meters.right * 2 shouldBe 6.meters.right
      3.meters.up * 2L shouldBe 6.meters.up
      3.meters.right * 0.5f shouldBe 1.5.meters.right
      3.meters.up * 0.5 shouldBe 1.5.meters.up
    }

    test("times goes toward the other side for a negative factor") {
      3.meters.right * -2 shouldBe 6.meters.left
      3.meters.up * -0.5 shouldBe 1.5.meters.down
    }

    test("times rejects a factor that is not finite") {
      shouldThrow<IllegalArgumentException> { 1.meters.right * Double.NaN }
      shouldThrow<IllegalArgumentException> {
        1.meters.up * Float.POSITIVE_INFINITY
      }
    }

    test("times rejects a product beyond the Float range") {
      shouldThrow<ArithmeticException> { Float.MAX_VALUE.meters.right * 2 }
      shouldThrow<ArithmeticException> { 1e20.meters.up * Long.MAX_VALUE }
    }

    test("div scales down along the axis") {
      3.meters.right / 2 shouldBe 1.5.meters.right
      3.meters.up / 2L shouldBe 1.5.meters.up
      3.meters.right / 0.5f shouldBe 6.meters.right
      3.meters.up / -0.5 shouldBe 6.meters.down
    }

    test("div rejects zero and a divisor that is not finite") {
      shouldThrow<ArithmeticException> { 1.meters.right / 0 }
      shouldThrow<ArithmeticException> { 1.meters.up / -0.0 }
      shouldThrow<IllegalArgumentException> { 1.meters.right / Double.NaN }
    }

    test("div rejects a quotient beyond the Float range") {
      shouldThrow<ArithmeticException> { 1.meters.right / 1e-40 }
    }

    test("toString shows the positive direction with an arrow") {
      val left = 3.meters.left
      val down = 1.meters.down
      val right = 1.5.meters.right
      left.toString() shouldBe "→ -3.000 m"
      down.toString() shouldBe "↑ -1.000 m"
      right.toString(precision = 1) shouldBe "→ 1.5 m"
    }
  })
