package io.github.hhth24340005.gff2027.game2d

import io.github.hhth24340005.gff2027.engine2d.Canvas
import io.github.hhth24340005.gff2027.engine2d.Image
import io.github.hhth24340005.gff2027.engine2d.Layer
import io.github.hhth24340005.gff2027.engine2d.Pipeline
import io.github.hhth24340005.gff2027.engine2d.VertexCount
import io.github.hhth24340005.gff2027.engine2d.WgslStruct
import io.github.hhth24340005.gff2027.engine2d.WgslStructWriter
import io.github.hhth24340005.gff2027.engine2d.shader
import io.github.hhth24340005.gff2027.math.Displacement2
import io.github.hhth24340005.gff2027.math.OriginApi
import io.github.hhth24340005.gff2027.math.Position2
import io.github.hhth24340005.gff2027.math.centimeters
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Draws [image] at a few places on [canvas], whose window has [size] in
 * screen coordinates, until canceled.
 */
internal suspend fun drawSprites(
  canvas: Canvas,
  size: Pair<Int, Int>,
  image: Image,
) {
  val pipeline = Pipeline(SPRITE_SHADER)
  val camera = MutableStateFlow(cameraOver(size))
  val sprites = MutableStateFlow(spritesOf(image.size))
  val layer = Layer(pipeline, camera, MutableStateFlow(image), items = sprites)
  canvas.runRenderForever(MutableStateFlow([layer]))
}

// region internal

/** The part of the plane the window shows, centered at [center]. */
private class Camera(
  val center: Position2,
  val halfSize: Displacement2,
) : WgslStruct {
  override fun writeTo(
    writer: WgslStructWriter,
  ) {
    writer.write(center)
    writer.write(halfSize)
  }
}

/** An image whose bottom-left corner is at [position]. */
private class Sprite(
  val position: Position2,
  val size: Displacement2,
) : WgslStruct {
  override fun writeTo(
    writer: WgslStructWriter,
  ) {
    writer.write(position)
    writer.write(size)
  }
}

// A screen coordinate spans a centimeter
@OptIn(OriginApi::class)
private fun cameraOver(
  size: Pair<Int, Int>,
): Camera {
  val [width, height] = size
  return Camera(
    Position2.origin,
    (width / 2).centimeters.right + (height / 2).centimeters.up,
  )
}

// Three sprites in a row, the middle one at the origin
@OptIn(OriginApi::class)
private fun spritesOf(
  imageSize: Pair<Int, Int>,
): List<Sprite> {
  val [width, height] = imageSize
  val size =
    (width * SCALE).centimeters.right + (height * SCALE).centimeters.up
  val middle = Position2.origin - size / 2
  val gap = (width * SCALE * 2).centimeters
  return [
    Sprite(middle - gap.right, size),
    Sprite(middle, size),
    Sprite(middle + gap.right, size),
  ]
}

private const val SCALE = 8

private val SPRITE_SHADER =
  shader(
    """
    struct Camera {
      center: vec2f,
      half_size: vec2f,
    }

    struct Sprite {
      // The bottom-left corner
      position: vec2f,
      size: vec2f,
    }

    @group(0) @binding(0) var<storage, read> camera: Camera;
    @group(0) @binding(1) var sprite_image: texture_2d<f32>;
    @group(1) @binding(1) var sprite_sampler: sampler;
    @group(2) @binding(0) var<storage, read> sprites: array<Sprite>;

    struct VertexOutput {
      @builtin(position) position: vec4f,
      @location(0) uv: vec2f,
    }

    @vertex
    fn vs_main(
      @builtin(vertex_index) v: u32,
      @builtin(instance_index) i: u32,
    ) -> VertexOutput {
      let corners = array(
        vec2f(0.0, 0.0), vec2f(1.0, 0.0), vec2f(0.0, 1.0),
        vec2f(0.0, 1.0), vec2f(1.0, 0.0), vec2f(1.0, 1.0),
      );
      let corner = corners[v];
      let sprite = sprites[i];
      let point = sprite.position + corner * sprite.size;
      let position = (point - camera.center) / camera.half_size;
      // uv points y down, as images list their rows top first
      let uv = vec2f(corner.x, 1.0 - corner.y);
      return VertexOutput(vec4f(position, 0.0, 1.0), uv);
    }

    @fragment
    fn fs_main(@location(0) uv: vec2f) -> @location(0) vec4f {
      let color = textureSample(sprite_image, sprite_sampler, uv);
      if color.a < 0.5 {
        discard;
      }
      return color;
    }
    """.trimIndent(),
    VertexCount(6),
    instances = Sprite::class,
  ).bind<Camera>()
    .bind<Image>()

// endregion
