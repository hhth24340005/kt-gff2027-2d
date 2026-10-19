package io.github.hhth24340005.gff2027.game2d

import arrow.fx.coroutines.ResourceScope
import io.github.hhth24340005.gff2027.webgpu.BindGroup
import io.github.hhth24340005.gff2027.webgpu.BindGroupEntry
import io.github.hhth24340005.gff2027.webgpu.Buffer
import io.github.hhth24340005.gff2027.webgpu.Color
import io.github.hhth24340005.gff2027.webgpu.ColorTargetState
import io.github.hhth24340005.gff2027.webgpu.Device
import io.github.hhth24340005.gff2027.webgpu.FragmentState
import io.github.hhth24340005.gff2027.webgpu.Instance
import io.github.hhth24340005.gff2027.webgpu.LoadOp
import io.github.hhth24340005.gff2027.webgpu.RenderPassColorAttachment
import io.github.hhth24340005.gff2027.webgpu.RenderPipeline
import io.github.hhth24340005.gff2027.webgpu.Surface
import io.github.hhth24340005.gff2027.webgpu.TextureFormat
import io.github.hhth24340005.gff2027.webgpu.VertexState
import io.github.hhth24340005.gff2027.webgpu.configure
import io.github.hhth24340005.gff2027.webgpu.createBindGroup
import io.github.hhth24340005.gff2027.webgpu.createBuffer
import io.github.hhth24340005.gff2027.webgpu.createCommandEncoder
import io.github.hhth24340005.gff2027.webgpu.createRenderPipeline
import io.github.hhth24340005.gff2027.webgpu.createSampler
import io.github.hhth24340005.gff2027.webgpu.createShaderModule
import io.github.hhth24340005.gff2027.webgpu.createTexture
import io.github.hhth24340005.gff2027.webgpu.createView
import io.github.hhth24340005.gff2027.webgpu.draw
import io.github.hhth24340005.gff2027.webgpu.finish
import io.github.hhth24340005.gff2027.webgpu.getBindGroupLayout
import io.github.hhth24340005.gff2027.webgpu.getCurrentTexture
import io.github.hhth24340005.gff2027.webgpu.present
import io.github.hhth24340005.gff2027.webgpu.renderPass
import io.github.hhth24340005.gff2027.webgpu.requestAdapter
import io.github.hhth24340005.gff2027.webgpu.requestDevice
import io.github.hhth24340005.gff2027.webgpu.resourceCoroutineScope
import io.github.hhth24340005.gff2027.webgpu.setBindGroup
import io.github.hhth24340005.gff2027.webgpu.setPipeline
import io.github.hhth24340005.gff2027.webgpu.submit
import io.github.hhth24340005.gff2027.webgpu.supportedFormats
import io.github.hhth24340005.gff2027.webgpu.writeBuffer
import io.github.hhth24340005.gff2027.webgpu.writeTexture
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.coroutines.ContinuationInterceptor
import kotlin.time.Duration.Companion.seconds

/** An image whose texels are ARGB ints in sRGB, top row first. */
internal class Image(
  val size: Pair<Int, Int>,
  val argb: IntArray,
)

/**
 * Draws [image] on [surface] every frame until canceled. Configures [surface]
 * on [mainThread], as some platforms allow changing a window only from its
 * thread.
 */
context(_: ResourceScope)
internal suspend fun drawSprite(
  instance: Instance,
  surface: Surface,
  size: Pair<Int, Int>,
  mainThread: ContinuationInterceptor,
  image: Image,
) {
  val adapter =
    instance.requestAdapter(surface) ?: error("No GPU adapter is available")
  val device = adapter.requestDevice()
  val format =
    surface.supportedFormats(adapter).firstOrNull()
      ?: error("The surface supports no format")
  withContext(mainThread) {
    surface.configure(device, format, size = size)
  }
  val module = device.createShaderModule(wgsl = SPRITE_WGSL)
  val pipeline =
    device.createRenderPipeline(
      VertexState(module, entryPoint = "vs_main"),
      FragmentState(
        module,
        [ColorTargetState(format)],
        entryPoint = "fs_main",
      ),
    )
  val bindGroup = spriteBindGroup(device, pipeline, image, size)

  while (true) {
    resourceCoroutineScope { drawFrame(surface, device, pipeline, bindGroup) }
    delay(1.seconds / 60)
  }
}

// region internal

context(_: ResourceScope)
private suspend fun spriteBindGroup(
  device: Device,
  pipeline: RenderPipeline,
  image: Image,
  canvasSize: Pair<Int, Int>,
): BindGroup {
  val [width, _] = image.size
  val texture =
    device.createTexture(
      size = image.size,
      // Matches ARGB ints, which lie in memory as B, G, R, A
      format = TextureFormat.BGRA8UnormSrgb,
    )
  device.queue.writeTexture(texture, image.argb, elementsPerRow = width)
  return device.createBindGroup(
    pipeline.getBindGroupLayout(0),
    [
      BindGroupEntry(0, texture.createView()),
      BindGroupEntry(1, device.createSampler()),
      BindGroupEntry(2, centeredPlacement(device, canvasSize, image.size)),
    ],
  )
}

context(_: ResourceScope)
private suspend fun centeredPlacement(
  device: Device,
  canvasSize: Pair<Int, Int>,
  imageSize: Pair<Int, Int>,
): Buffer {
  val [canvasWidth, canvasHeight] = canvasSize
  val [imageWidth, imageHeight] = imageSize
  val width = (imageWidth * SCALE).toFloat()
  val height = (imageHeight * SCALE).toFloat()
  val placement =
    floatArrayOf(
      canvasWidth.toFloat(),
      canvasHeight.toFloat(),
      (canvasWidth - width) / 2,
      (canvasHeight - height) / 2,
      width,
      height,
    )
  val buffer = device.createBuffer(size = placement.size * Float.SIZE_BYTES)
  device.queue.writeBuffer(buffer, placement)
  return buffer
}

context(_: ResourceScope)
private suspend fun drawFrame(
  surface: Surface,
  device: Device,
  pipeline: RenderPipeline,
  bindGroup: BindGroup,
) {
  val texture = surface.getCurrentTexture() ?: return
  val view = texture.createView()
  val encoder = device.createCommandEncoder()
  encoder.renderPass(
    [RenderPassColorAttachment(view, LoadOp.Clear(BACKGROUND))],
  ) {
    setPipeline(pipeline)
    setBindGroup(0, bindGroup)
    draw(vertexCount = 6)
  }
  device.queue.submit([encoder.finish()])
  surface.present()
}

private val BACKGROUND = Color(red = 0.1, green = 0.1, blue = 0.15)

private const val SCALE = 16

// endregion
