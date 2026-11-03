package io.github.hhth24340005.gff2027.engine2d

import arrow.fx.coroutines.ResourceScope
import io.github.hhth24340005.gff2027.webgpu.BindGroupLayout
import io.github.hhth24340005.gff2027.webgpu.BindGroupLayoutEntry
import io.github.hhth24340005.gff2027.webgpu.ColorTargetState
import io.github.hhth24340005.gff2027.webgpu.FragmentState
import io.github.hhth24340005.gff2027.webgpu.RenderPipeline
import io.github.hhth24340005.gff2027.webgpu.VertexState
import io.github.hhth24340005.gff2027.webgpu.createBindGroupLayout
import io.github.hhth24340005.gff2027.webgpu.createPipelineLayout
import io.github.hhth24340005.gff2027.webgpu.createRenderPipeline
import io.github.hhth24340005.gff2027.webgpu.createShaderModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Compiles pipelines for the GPU that draws to its canvas. Obtained from
 * [Canvas.compiler], and can be handed around apart from the canvas.
 */
public class Compiler internal constructor(internal val canvas: Canvas) {
  /**
   * Compiles [pipelines] in parallel, and returns when all are done. The
   * canvas keeps them until it closes, so drawing them does not wait to
   * compile.
   */
  public suspend fun compile(
    pipelines: List<Pipeline<*, *>>,
  ): Unit =
    canvas.compileAll(pipelines)
}


// region internal

internal class CompiledPipeline(
  val pipeline: RenderPipeline,
  val bindGroupLayouts: List<BindGroupLayout>,
)

/**
 * Returns [pipeline] compiled, compiling it on another thread if no one
 * has.
 */
internal suspend fun Canvas.compiled(
  pipeline: Pipeline<*, *>,
): CompiledPipeline {
  val cached = compiledPipelinesLock.withLock { compiledPipelines[pipeline] }
  if (cached != null) {
    return cached.value
  }
  return withContext(Dispatchers.Default) {
    val made = allocate { compile(pipeline) }
    // Caches or releases made even if canceled from here
    withContext(NonCancellable) { keep(pipeline, made) }
  }
}

private suspend fun Canvas.compileAll(
  pipelines: List<Pipeline<*, *>>,
) {
  coroutineScope {
    pipelines.distinct().forEach { pipeline ->
      launch { val _ = compiled(pipeline) }
    }
  }
}

// Caches made, or releases it if another compile of the same pipeline
// finished first or the canvas has closed
private suspend fun Canvas.keep(
  pipeline: Pipeline<*, *>,
  made: Allocated<CompiledPipeline>,
): CompiledPipeline {
  val kept =
    compiledPipelinesLock.withLock {
      if (compiledPipelinesClosed) {
        made.release()
        error("The canvas has closed")
      }
      compiledPipelines.getOrPut(pipeline) { made }
    }
  if (kept !== made) {
    made.release()
  }
  return kept.value
}

context(_: ResourceScope)
private suspend fun Canvas.compile(
  pipeline: Pipeline<*, *>,
): CompiledPipeline {
  val shader = pipeline.shader
  val module = device.createShaderModule(wgsl = shader.wgsl)
  val bindGroupLayouts =
    bindGroupLayoutEntries(shader).map { device.createBindGroupLayout(it) }
  val renderPipeline =
    device.createRenderPipeline(
      device.createPipelineLayout(bindGroupLayouts),
      VertexState(module, entryPoint = Auto),
      FragmentState(
        module,
        [ColorTargetState(pipeline.format.textureFormat(format))],
        entryPoint = Auto,
      ),
    )
  return CompiledPipeline(renderPipeline, bindGroupLayouts)
}

private fun bindGroupLayoutEntries(
  shader: Shader,
): List<List<BindGroupLayoutEntry>> {
  val types = shader.bindingTypes.withIndex()
  val resources =
    types.map { (index, value) ->
      BindGroupLayoutEntry(index, value)
    }
  val samplers =
    types
      .filter { it.value == Texture }
      .map { BindGroupLayoutEntry(it.index, Sampler) }
  val instances =
    if (shader.hasInstances) {
      [BindGroupLayoutEntry(0, ReadOnlyStorage)]
    } else {
      []
    }
  return [resources, samplers, instances]
}

// endregion
