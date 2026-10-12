import arrow.fx.coroutines.ResourceScope
import io.github.hhth24340005.gff2027.webgpu.Color
import io.github.hhth24340005.gff2027.webgpu.ColorTargetState
import io.github.hhth24340005.gff2027.webgpu.Device
import io.github.hhth24340005.gff2027.webgpu.FragmentState
import io.github.hhth24340005.gff2027.webgpu.Instance
import io.github.hhth24340005.gff2027.webgpu.LoadOp
import io.github.hhth24340005.gff2027.webgpu.RenderPassColorAttachment
import io.github.hhth24340005.gff2027.webgpu.RenderPipeline
import io.github.hhth24340005.gff2027.webgpu.Surface
import io.github.hhth24340005.gff2027.webgpu.VertexState
import io.github.hhth24340005.gff2027.webgpu.configure
import io.github.hhth24340005.gff2027.webgpu.createCommandEncoder
import io.github.hhth24340005.gff2027.webgpu.createRenderPipeline
import io.github.hhth24340005.gff2027.webgpu.createShaderModule
import io.github.hhth24340005.gff2027.webgpu.createView
import io.github.hhth24340005.gff2027.webgpu.draw
import io.github.hhth24340005.gff2027.webgpu.finish
import io.github.hhth24340005.gff2027.webgpu.getCurrentTexture
import io.github.hhth24340005.gff2027.webgpu.present
import io.github.hhth24340005.gff2027.webgpu.renderPass
import io.github.hhth24340005.gff2027.webgpu.requestAdapter
import io.github.hhth24340005.gff2027.webgpu.requestDevice
import io.github.hhth24340005.gff2027.webgpu.resourceCoroutineScope
import io.github.hhth24340005.gff2027.webgpu.setPipeline
import io.github.hhth24340005.gff2027.webgpu.submit
import io.github.hhth24340005.gff2027.webgpu.supportedFormats
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

/**
 * Draws a triangle on [surface] every frame until canceled. Configures
 * [surface] on [windowContext], as some platforms allow changing a window only
 * from its thread.
 */
context(_: ResourceScope)
internal suspend fun drawTriangle(
  instance: Instance,
  surface: Surface,
  size: Pair<Int, Int>,
  windowContext: CoroutineContext,
) {
  val adapter =
    instance.requestAdapter(surface) ?: error("No GPU adapter is available")
  val device = adapter.requestDevice()
  val format =
    surface.supportedFormats(adapter).firstOrNull()
      ?: error("The surface supports no format")
  withContext(windowContext) {
    surface.configure(device, format, size = size)
  }
  val module = device.createShaderModule(wgsl = SHADER)
  val pipeline =
    device.createRenderPipeline(
      VertexState(module, entryPoint = "vs_main"),
      FragmentState(
        module,
        [ColorTargetState(format)],
        entryPoint = "fs_main",
      ),
    )

  while (true) {
    resourceCoroutineScope { drawFrame(surface, device, pipeline) }
    delay(1.seconds / 60)
  }
}

context(_: ResourceScope)
private suspend fun drawFrame(
  surface: Surface,
  device: Device,
  pipeline: RenderPipeline,
) {
  val texture = surface.getCurrentTexture() ?: return
  val view = texture.createView()
  val encoder = device.createCommandEncoder()
  encoder.renderPass(
    [RenderPassColorAttachment(view, LoadOp.Clear(BACKGROUND))],
  ) {
    setPipeline(pipeline)
    draw(vertexCount = 3)
  }
  device.queue.submit([encoder.finish()])
  surface.present()
}

private val BACKGROUND = Color(red = 0.1, green = 0.1, blue = 0.15)

private const val SHADER =
  """
  @vertex
  fn vs_main(@builtin(vertex_index) i: u32) -> @builtin(position) vec4f {
    let positions = array(vec2f(0.0, 0.5), vec2f(-0.5, -0.5), vec2f(0.5, -0.5));
    return vec4f(positions[i], 0.0, 1.0);
  }

  @fragment
  fn fs_main() -> @location(0) vec4f {
    return vec4f(1.0, 0.4, 0.2, 1.0);
  }
  """.trimIndent()
