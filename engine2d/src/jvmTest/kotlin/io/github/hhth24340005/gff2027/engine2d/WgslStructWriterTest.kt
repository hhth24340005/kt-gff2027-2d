package io.github.hhth24340005.gff2027.engine2d

import io.github.hhth24340005.gff2027.math.Distance
import io.github.hhth24340005.gff2027.math.Position2
import io.github.hhth24340005.gff2027.math.meters
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Suppress("unused")
class WgslStructWriterTest :
  FunSpec({
    // struct { a: f32, b: vec2f }
    class ScalarFirst(
      val a: Distance,
      val b: Position2,
    ) : WgslStruct {
      override fun writeTo(
        writer: WgslStructWriter,
      ) {
        writer.write(a)
        writer.write(b)
      }
    }

    // struct { b: vec2f, a: f32 }
    class VectorFirst(
      val b: Position2,
      val a: Distance,
    ) : WgslStruct {
      override fun writeTo(
        writer: WgslStructWriter,
      ) {
        writer.write(b)
        writer.write(a)
      }
    }

    // struct { a: f32 }
    class Scalar(val a: Distance) : WgslStruct {
      override fun writeTo(
        writer: WgslStructWriter,
      ) {
        writer.write(a)
      }
    }

    val point = Position2.origin + 2.meters.right + 3.meters.down

    test("a vec2f starts at a multiple of 8 bytes") {
      wgslWords([ScalarFirst(1.meters, point)]).floats() shouldBe
        [1f, 0f, 2f, -3f]
    }

    test("a struct pads its end to its alignment") {
      wgslWords([VectorFirst(point, 1.meters)]).floats() shouldBe
        [2f, -3f, 1f, 0f]
    }

    test("an array lays its structs one after another") {
      wgslWords([Scalar(1.meters), Scalar(2.meters)]).floats() shouldBe
        [1f, 2f]
      wgslWords([VectorFirst(point, 1.meters), VectorFirst(point, 2.meters)])
        .floats() shouldBe [2f, -3f, 1f, 0f, 2f, -3f, 2f, 0f]
    }
  })

private fun IntArray.floats(): List<Float> =
  map { Float.fromBits(it) }
