package io.github.hhth24340005.math

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Suppress("unused")
class PlaneTest :
  FunSpec({
    val origin = Position2.origin

    test("horizontal and vertical combine in either order") {
      3.meters.right + 1.meters.down shouldBe 1.meters.down + 3.meters.right
      3.meters.right - 1.meters.up shouldBe 3.meters.right + 1.meters.down
      1.meters.up - 3.meters.right shouldBe 3.meters.left + 1.meters.up
    }

    test("components come back out") {
      val move = 3.meters.right + 1.meters.down
      move.horizontal shouldBe 3.meters.right
      move.vertical shouldBe 1.meters.down
    }

    test("displacements add and scale per component") {
      val move = 3.meters.right + 1.meters.down
      move + move shouldBe move * 2
      move - move shouldBe Displacement2.zero
      -move shouldBe 3.meters.left + 1.meters.up
      move / 2 shouldBe 1.5.meters.right + 0.5.meters.down
      move + 1.meters.up shouldBe 3.meters.right + 0.meters.up
    }

    test("div rejects zero") {
      shouldThrow<ArithmeticException> { (1.meters.right + 1.meters.up) / 0 }
    }

    test("a position moves by a displacement") {
      val p = origin + 3.meters.right + 1.meters.down
      p - origin shouldBe 3.meters.right + 1.meters.down
      p - 3.meters.right shouldBe origin + 1.meters.down
    }

    test("the difference of positions moves one onto the other") {
      val a = origin + 3.meters.right
      val b = origin + 1.meters.up
      b + (a - b) shouldBe a
    }

    test("toString shows both components") {
      val move = 3.meters.right + 1.meters.down
      move.toString() shouldBe "(→ 3.000 m, ↑ -1.000 m)"
      (origin + move).toString(precision = 1) shouldBe
        "origin + (→ 3.0 m, ↑ -1.0 m)"
    }
  })
