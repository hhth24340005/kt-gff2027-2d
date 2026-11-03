package io.github.hhth24340005.gff2027.engine2d

import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.resourceScope
import io.github.hhth24340005.gff2027.webgpu.BindGroup
import io.github.hhth24340005.gff2027.webgpu.BindGroupEntry
import io.github.hhth24340005.gff2027.webgpu.Buffer
import io.github.hhth24340005.gff2027.webgpu.BufferBinding
import io.github.hhth24340005.gff2027.webgpu.Color
import io.github.hhth24340005.gff2027.webgpu.CurrentTexture
import io.github.hhth24340005.gff2027.webgpu.Device
import io.github.hhth24340005.gff2027.webgpu.FilterMode
import io.github.hhth24340005.gff2027.webgpu.LoadOp
import io.github.hhth24340005.gff2027.webgpu.RenderBundle
import io.github.hhth24340005.gff2027.webgpu.RenderPassColorAttachment
import io.github.hhth24340005.gff2027.webgpu.Sampler
import io.github.hhth24340005.gff2027.webgpu.Surface
import io.github.hhth24340005.gff2027.webgpu.Texture
import io.github.hhth24340005.gff2027.webgpu.TextureFormat
import io.github.hhth24340005.gff2027.webgpu.TextureView
import io.github.hhth24340005.gff2027.webgpu.createBindGroup
import io.github.hhth24340005.gff2027.webgpu.createBuffer
import io.github.hhth24340005.gff2027.webgpu.createCommandEncoder
import io.github.hhth24340005.gff2027.webgpu.createRenderBundleEncoder
import io.github.hhth24340005.gff2027.webgpu.createSampler
import io.github.hhth24340005.gff2027.webgpu.createTexture
import io.github.hhth24340005.gff2027.webgpu.createView
import io.github.hhth24340005.gff2027.webgpu.draw
import io.github.hhth24340005.gff2027.webgpu.drawIndirect
import io.github.hhth24340005.gff2027.webgpu.executeBundles
import io.github.hhth24340005.gff2027.webgpu.finish
import io.github.hhth24340005.gff2027.webgpu.getCurrentTexture
import io.github.hhth24340005.gff2027.webgpu.present
import io.github.hhth24340005.gff2027.webgpu.reference
import io.github.hhth24340005.gff2027.webgpu.renderPass
import io.github.hhth24340005.gff2027.webgpu.setBindGroup
import io.github.hhth24340005.gff2027.webgpu.setPipeline
import io.github.hhth24340005.gff2027.webgpu.submit
import io.github.hhth24340005.gff2027.webgpu.writeBuffer
import io.github.hhth24340005.gff2027.webgpu.writeTexture
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Draws to a surface with its own GPU device. Only its drawing thread uses
 * the device, apart from its [compiler].
 */
public class Canvas internal constructor(
  internal val exposures: Flow<Unit>,
  internal val surface: Surface,
  internal val device: Device,
  internal val format: TextureFormat,
) {
  /** Compiles pipelines for the GPU that draws to this canvas. */
  public val compiler: Compiler = Compiler(this)

  internal val compiledPipelines =
    mutableMapOf<Pipeline<*, *>, Allocated<CompiledPipeline>>()
  internal val compiledPipelinesLock = Mutex()

  // Guarded by compiledPipelinesLock, and keeps compiles from caching after
  // releaseAll
  internal var compiledPipelinesClosed = false

  // Runs every draw in order on one thread at a time
  private val drawDispatcher: CoroutineDispatcher =
    Dispatchers.Default.limitedParallelism(1)

  // Used only on drawDispatcher
  private val layerBundles = mutableMapOf<Layer<*>, LayerBundle>()
  private val uploads = mutableMapOf<StateFlow<Any>, Upload>()

  /**
   * Draws [layers] in order whenever the list, a resource of its layers, or
   * the surface's exposure changes, and keeps the frame on the surface until
   * the next change. Runs until canceled.
   */
  public suspend fun runRenderForever(
    layers: StateFlow<List<Layer<ColorFormat.Canvas>>>,
  ): Nothing =
    runRenderImpl(layers)

  // region internal

  internal suspend fun releaseAll() {
    layerBundles.values.forEach { it.release() }
    layerBundles.clear()
    uploads.values.forEach { it.gpu.release() }
    uploads.clear()
    compiledPipelinesLock.withLock {
      compiledPipelinesClosed = true
      compiledPipelines.values.forEach { it.release() }
      compiledPipelines.clear()
    }
  }

  private suspend fun runRenderImpl(
    layers: StateFlow<List<Layer<ColorFormat.Canvas>>>,
  ): Nothing {
    withContext(drawDispatcher) {
      @OptIn(ExperimentalCoroutinesApi::class)
      val resourceChanges =
        layers.flatMapLatest { list ->
          // Skips the values already on the GPU, such as the one a StateFlow
          // sends on subscribing, which would draw again every time the
          // subscriptions switch
          list
            .flatMap { it.flows }
            .distinct()
            .map { flow -> flow.filter { uploads[flow]?.value !== it } }
            .merge()
        }
      merge(layers, resourceChanges, exposures)
        .conflate()
        .collect { draw(layers.value) }
      awaitCancellation()
    }
  }

  /** A resource on the GPU, made from the value of a [StateFlow]. */
  private sealed interface GpuResource

  // Shaders see only the first size bytes
  private class StorageBuffer(
    val buffer: Buffer,
    val capacity: Int,
    val size: Int,
  ) : GpuResource

  private class ImageTexture(
    val texture: Texture,
    val view: TextureView,
    val sampler: Sampler,
    val pixels: Pixels,
  ) : GpuResource

  /**
   * [gpu] made from [value], which it compares by identity. [value] is a
   * [ShaderResource] or a list of instances, which [Canvas.uploads] keeps in
   * one map, so this is not generic.
   */
  private class Upload(
    val value: Any,
    val gpu: Allocated<GpuResource>,
  )

  /** The GPU objects a [Layer] draws with. Used only on the draw thread. */
  private class LayerBundle(val indirect: Allocated<Buffer>?) {
    var instanceCount: Int? = null
    var bundle: Allocated<RenderBundle>? = null

    // The bundle binds these, so it needs encoding again when they change
    var dependencies: List<Any> = []

    suspend fun release() {
      indirect?.release()
      bundle?.release()
    }
  }

  private suspend fun draw(
    layers: List<Layer<ColorFormat.Canvas>>,
  ) {
    // Releases right away what the layers no longer use, even if they may use
    // it again
    val flows = layers.flatMap { it.flows }.toSet()
    (uploads.keys - flows).forEach {
      uploads
        .getValue(it)
        .gpu
        .release()
    }
    uploads.keys.retainAll(flows)
    val layerSet = layers.toSet()
    (layerBundles.keys - layerSet).forEach {
      layerBundles.getValue(it).release()
    }
    layerBundles.keys.retainAll(layerSet)

    val bundles = layers.map { bundleOf(it) }
    resourceScope { present(bundles) }
  }

  context(_: ResourceScope)
  private suspend fun present(
    bundles: List<RenderBundle>,
  ) {
    // Drops the frame while the window is hidden, as it draws again when shown
    val texture = currentTexture() ?: return
    val encoder = device.createCommandEncoder()
    val view = texture.createView()
    encoder.renderPass(
      [RenderPassColorAttachment(view, LoadOp.Clear(BACKGROUND))],
    ) {
      executeBundles(bundles)
    }
    device.queue.submit([encoder.finish()])
    surface.present()
  }

  // Tries again when no texture became free in time
  context(_: ResourceScope)
  private suspend fun currentTexture(): Texture? {
    while (true) {
      when (val current = surface.getCurrentTexture()) {
        is CurrentTexture.Success -> {
          return current.texture
        }

        CurrentTexture.Occluded -> {
          return null
        }

        CurrentTexture.Timeout -> {}
      }
    }
  }

  private suspend fun bundleOf(
    layer: Layer<ColorFormat.Canvas>,
  ): RenderBundle {
    val compiled = compiled(layer.pipeline)
    val resources = layer.resources.map { uploadIfChanged(it) }
    val instances = layer.items?.let { uploadIfChanged(it) as StorageBuffer }
    val layerBundle =
      layerBundles.getOrPut(layer) {
        LayerBundle(layer.items?.let { allocate { indirectBuffer() } })
      }
    val instanceCount =
      layer.items
        ?.value
        ?.size
    if (layerBundle.indirect != null &&
      instanceCount != layerBundle.instanceCount
    ) {
      val arguments =
        intArrayOf(
          layer.pipeline.shader.vertexCount, instanceCount ?: 0, 0, 0,
        )
      device.queue.writeBuffer(layerBundle.indirect.value, arguments)
      layerBundle.instanceCount = instanceCount
    }
    // The instances are bound whole, as the indirect buffer counts them
    val dependencies = resources + listOfNotNull(instances?.buffer)
    val bundle = layerBundle.bundle
    if (bundle != null && dependencies == layerBundle.dependencies) {
      return bundle.value
    }
    bundle?.release()
    val encoded =
      encodeBundle(layer, compiled, resources, instances, layerBundle.indirect)
    layerBundle.bundle = encoded
    layerBundle.dependencies = dependencies
    return encoded.value
  }

  private suspend fun encodeBundle(
    layer: Layer<ColorFormat.Canvas>,
    compiled: CompiledPipeline,
    resources: List<GpuResource>,
    instances: StorageBuffer?,
    indirect: Allocated<Buffer>?,
  ): Allocated<RenderBundle> =
    resourceScope {
      val encoder = device.createRenderBundleEncoder([format])
      encoder.setPipeline(compiled.pipeline)
      bindGroups(compiled, resources, instances).forEachIndexed {
        index,
        group,
        ->
        encoder.setBindGroup(index, group)
      }
      if (indirect != null) {
        encoder.drawIndirect(indirect.value)
      } else {
        encoder.draw(vertexCount = layer.pipeline.shader.vertexCount)
      }
      // The bundle keeps the bind groups alive after this scope exits
      encoder
        .finish()
        .reference()
        .toAllocated()
    }

  context(_: ResourceScope)
  private suspend fun bindGroups(
    compiled: CompiledPipeline,
    resources: List<GpuResource>,
    instances: StorageBuffer?,
  ): List<BindGroup> {
    val indexed = resources.withIndex()
    val groups =
      [
        indexed.map { (index, value) ->
          BindGroupEntry(index, value.binding)
        },
        indexed.mapNotNull { [index, value] ->
          (value as? ImageTexture)?.let { BindGroupEntry(index, it.sampler) }
        },
        listOfNotNull(instances?.let { BindGroupEntry(0, it.buffer) }),
      ]
    return groups.mapIndexed { index, entries ->
      device.createBindGroup(compiled.bindGroupLayouts[index], entries)
    }
  }

  private val GpuResource.binding
    get() =
      when (this) {
        is StorageBuffer -> BufferBinding(buffer, size)
        is ImageTexture -> view
      }

  private suspend fun uploadIfChanged(
    flow: StateFlow<Any>,
  ): GpuResource {
    val value = flow.value
    val previous = uploads[flow]
    if (previous != null && previous.value === value) {
      return previous.gpu.value
    }
    val gpu =
      if (value is Pixels) {
        uploadPixels(value, previous?.gpu)
      } else {
        writeOrGrowStorage(storageWords(value), previous?.gpu)
      }
    uploads[flow] = Upload(value, gpu)
    return gpu.value
  }

  // Writes into previous if the words fit in it, or else into a buffer twice
  // as large
  private suspend fun writeOrGrowStorage(
    words: IntArray,
    previous: Allocated<GpuResource>?,
  ): Allocated<GpuResource> {
    val size = words.size * Int.SIZE_BYTES
    val old = previous?.value as? StorageBuffer
    val gpu =
      if (old != null && size <= old.capacity) {
        if (size == old.size) {
          previous
        } else {
          previous.map { StorageBuffer(old.buffer, old.capacity, size) }
        }
      } else {
        previous?.release()
        val capacity =
          maxOf(size, MIN_STORAGE_SIZE, (old?.capacity ?: 0) * 2)
        allocate { storageBuffer(capacity, size) }
      }
    if (words.isNotEmpty()) {
      device.queue.writeBuffer((gpu.value as StorageBuffer).buffer, words)
    }
    return gpu
  }

  // Writes into the previous texture if it has the same size and filter
  private suspend fun uploadPixels(
    pixels: Pixels,
    previous: Allocated<GpuResource>?,
  ): Allocated<GpuResource> {
    val old = (previous?.value as? ImageTexture)?.pixels
    val gpu =
      if (old != null &&
        old.size == pixels.size &&
        old.filter == pixels.filter
      ) {
        previous
      } else {
        previous?.release()
        allocate { imageTexture(pixels) }
      }
    device.queue.writeTexture(
      (gpu.value as ImageTexture).texture,
      pixels.argb,
      elementsPerRow = pixels.size.first,
    )
    return gpu
  }

  context(_: ResourceScope)
  private suspend fun storageBuffer(
    capacity: Int,
    size: Int,
  ): GpuResource =
    StorageBuffer(
      device.createBuffer(
        size = capacity,
        usage = [Storage, CopyDst],
      ),
      capacity,
      size,
    )

  context(_: ResourceScope)
  private suspend fun imageTexture(
    pixels: Pixels,
  ): GpuResource {
    val texture =
      device.createTexture(
        size = pixels.size,
        // Matches ARGB ints, which lie in memory as B, G, R, A
        format = BGRA8UnormSrgb,
      )
    val filter = pixels.filter.filterMode
    val sampler = device.createSampler(magFilter = filter, minFilter = filter)
    return ImageTexture(texture, texture.createView(), sampler, pixels)
  }

  // Holds the arguments of drawIndirect
  context(_: ResourceScope)
  private suspend fun indirectBuffer(): Buffer =
    device.createBuffer(
      size = INDIRECT_SIZE,
      usage = [Indirect, CopyDst],
    )

  // endregion
}

// region internal

private fun storageWords(
  value: Any,
): IntArray =
  when (value) {
    is WgslStruct -> wgslWords([value])
    is WgslArray<*> -> wgslWords(value.elements)
    is List<*> -> wgslWords(value.filterIsInstance<WgslStruct>())
    else -> error("Not a resource: $value")
  }

private val ImageFilter.filterMode: FilterMode
  get() =
    when (this) {
      Nearest -> Nearest
      Linear -> Linear
    }

private val BACKGROUND = Color(red = 0.0, green = 0.0, blue = 0.0)

// A runtime-sized array needs room for at least one element
private const val MIN_STORAGE_SIZE = 256

// vertexCount, instanceCount, firstVertex and firstInstance as u32
private const val INDIRECT_SIZE = 4 * Int.SIZE_BYTES

// endregion
