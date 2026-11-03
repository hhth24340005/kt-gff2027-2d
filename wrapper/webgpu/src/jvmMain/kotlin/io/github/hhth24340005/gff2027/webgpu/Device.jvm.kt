package io.github.hhth24340005.gff2027.webgpu

import wgpu_native.WGPUBindGroupDescriptor
import wgpu_native.WGPUBindGroupEntry
import wgpu_native.WGPUBindGroupLayoutDescriptor
import wgpu_native.WGPUBindGroupLayoutEntry
import wgpu_native.WGPUBufferBindingLayout
import wgpu_native.WGPUBufferDescriptor
import wgpu_native.WGPUChainedStruct
import wgpu_native.WGPUColorTargetState
import wgpu_native.WGPUExtent3D
import wgpu_native.WGPUFragmentState
import wgpu_native.WGPUMultisampleState
import wgpu_native.WGPUPipelineLayoutDescriptor
import wgpu_native.WGPUPrimitiveState
import wgpu_native.WGPURenderBundleEncoderDescriptor
import wgpu_native.WGPURenderPipelineDescriptor
import wgpu_native.WGPUSamplerBindingLayout
import wgpu_native.WGPUSamplerDescriptor
import wgpu_native.WGPUShaderModuleDescriptor
import wgpu_native.WGPUShaderSourceWGSL
import wgpu_native.WGPUTextureBindingLayout
import wgpu_native.WGPUTextureDescriptor
import wgpu_native.WGPUVertexState
import wgpu_native.webgpu_h.WGPUAddressMode_ClampToEdge
import wgpu_native.webgpu_h.WGPUBufferBindingType_ReadOnlyStorage
import wgpu_native.webgpu_h.WGPUMipmapFilterMode_Nearest
import wgpu_native.webgpu_h.WGPUPrimitiveTopology_TriangleList
import wgpu_native.webgpu_h.WGPUSType_ShaderSourceWGSL
import wgpu_native.webgpu_h.WGPUSamplerBindingType_Filtering
import wgpu_native.webgpu_h.WGPUTextureDimension_2D
import wgpu_native.webgpu_h.WGPUTextureSampleType_Float
import wgpu_native.webgpu_h.WGPUTextureViewDimension_2D
import wgpu_native.webgpu_h.WGPU_WHOLE_SIZE
import wgpu_native.webgpu_h.wgpuDeviceCreateBindGroup
import wgpu_native.webgpu_h.wgpuDeviceCreateBindGroupLayout
import wgpu_native.webgpu_h.wgpuDeviceCreateBuffer
import wgpu_native.webgpu_h.wgpuDeviceCreateCommandEncoder
import wgpu_native.webgpu_h.wgpuDeviceCreatePipelineLayout
import wgpu_native.webgpu_h.wgpuDeviceCreateRenderBundleEncoder
import wgpu_native.webgpu_h.wgpuDeviceCreateRenderPipeline
import wgpu_native.webgpu_h.wgpuDeviceCreateSampler
import wgpu_native.webgpu_h.wgpuDeviceCreateShaderModule
import wgpu_native.webgpu_h.wgpuDeviceCreateTexture
import wgpu_native.webgpu_h.wgpuDeviceGetQueue
import wgpu_native.webgpu_h.wgpuDeviceRelease
import wgpu_native.webgpu_h.wgpuQueueRelease
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySegment.NULL
import java.lang.foreign.ValueLayout.ADDRESS
import java.lang.foreign.ValueLayout.JAVA_INT

public actual class Device internal constructor(
  internal val handle: MemorySegment,
) {
  public actual val queue: Queue = Queue(wgpuDeviceGetQueue(handle))
}

// region internal

internal actual fun Device.createShaderModuleImpl(
  wgsl: String,
  label: String?,
): ShaderModule =
  Arena.ofConfined().use { arena ->
    ShaderModule(
      wgpuDeviceCreateShaderModule(
        handle,
        shaderModuleDescriptor(arena, wgsl, label),
      ),
    )
  }

internal actual fun Device.createBindGroupLayoutImpl(
  entries: List<BindGroupLayoutEntry>,
  label: String?,
): BindGroupLayout =
  Arena.ofConfined().use { arena ->
    BindGroupLayout(
      wgpuDeviceCreateBindGroupLayout(
        handle,
        bindGroupLayoutDescriptor(arena, entries, label),
      ),
    )
  }

internal actual fun Device.createPipelineLayoutImpl(
  bindGroupLayouts: List<BindGroupLayout>,
  label: String?,
): PipelineLayout =
  Arena.ofConfined().use { arena ->
    PipelineLayout(
      wgpuDeviceCreatePipelineLayout(
        handle,
        pipelineLayoutDescriptor(arena, bindGroupLayouts, label),
      ),
    )
  }

internal actual fun Device.createRenderPipelineImpl(
  layout: PipelineLayout,
  vertex: VertexState,
  fragment: FragmentState,
  label: String?,
): RenderPipeline =
  Arena.ofConfined().use { arena ->
    RenderPipeline(
      wgpuDeviceCreateRenderPipeline(
        handle,
        renderPipelineDescriptor(arena, layout, vertex, fragment, label),
      ),
    )
  }

internal actual fun Device.createBufferImpl(
  size: Int,
  usage: Set<BufferUsage>,
  label: String?,
): Buffer =
  Arena.ofConfined().use { arena ->
    Buffer(
      wgpuDeviceCreateBuffer(
        handle,
        bufferDescriptor(arena, size, usage, label),
      ),
    )
  }

internal actual fun Device.createTextureImpl(
  size: Pair<Int, Int>,
  format: TextureFormat,
  label: String?,
): Texture =
  Arena.ofConfined().use { arena ->
    Texture(
      wgpuDeviceCreateTexture(
        handle,
        textureDescriptor(arena, size, format, label),
      ),
    )
  }

internal actual fun Device.createSamplerImpl(
  magFilter: FilterMode,
  minFilter: FilterMode,
  label: String?,
): Sampler =
  Arena.ofConfined().use { arena ->
    Sampler(
      wgpuDeviceCreateSampler(
        handle,
        samplerDescriptor(arena, magFilter, minFilter, label),
      ),
    )
  }

internal actual fun Device.createBindGroupImpl(
  layout: BindGroupLayout,
  entries: List<BindGroupEntry>,
  label: String?,
): BindGroup =
  Arena.ofConfined().use { arena ->
    BindGroup(
      wgpuDeviceCreateBindGroup(
        handle,
        bindGroupDescriptor(arena, layout, entries, label),
      ),
    )
  }

internal actual fun Device.createCommandEncoderImpl(): CommandEncoder =
  CommandEncoder(wgpuDeviceCreateCommandEncoder(handle, NULL))

internal actual fun Device.createRenderBundleEncoderImpl(
  colorFormats: List<TextureFormat>,
  label: String?,
): RenderBundleEncoder =
  Arena.ofConfined().use { arena ->
    RenderBundleEncoder(
      wgpuDeviceCreateRenderBundleEncoder(
        handle,
        renderBundleEncoderDescriptor(arena, colorFormats, label),
      ),
    )
  }

internal actual fun Device.release() {
  wgpuQueueRelease(queue.handle)
  wgpuDeviceRelease(handle)
}

private fun shaderModuleDescriptor(
  arena: Arena,
  wgsl: String,
  label: String?,
): MemorySegment {
  val descriptor = WGPUShaderModuleDescriptor.allocate(arena)
  WGPUShaderModuleDescriptor.nextInChain(descriptor, wgslSource(arena, wgsl))
  if (label != null) {
    WGPUShaderModuleDescriptor.label(descriptor).setStringView(arena, label)
  }
  return descriptor
}

private fun wgslSource(
  arena: Arena,
  wgsl: String,
): MemorySegment {
  val source = WGPUShaderSourceWGSL.allocate(arena)
  WGPUChainedStruct.sType(
    WGPUShaderSourceWGSL.chain(source),
    WGPUSType_ShaderSourceWGSL(),
  )
  WGPUShaderSourceWGSL.code(source).setStringView(arena, wgsl)
  return source
}

private fun bindGroupLayoutDescriptor(
  arena: Arena,
  entries: List<BindGroupLayoutEntry>,
  label: String?,
): MemorySegment {
  val descriptor = WGPUBindGroupLayoutDescriptor.allocate(arena)
  if (label != null) {
    WGPUBindGroupLayoutDescriptor.label(descriptor).setStringView(arena, label)
  }
  WGPUBindGroupLayoutDescriptor.entryCount(descriptor, entries.size.toLong())
  WGPUBindGroupLayoutDescriptor
    .entries(descriptor, bindGroupLayoutEntries(arena, entries))
  return descriptor
}

// Zeroed fields leave the other binding types as BindingNotUsed
private fun bindGroupLayoutEntries(
  arena: Arena,
  entries: List<BindGroupLayoutEntry>,
): MemorySegment {
  val structs =
    WGPUBindGroupLayoutEntry.allocateArray(entries.size.toLong(), arena)
  entries.forEachIndexed { index, entry ->
    val struct = WGPUBindGroupLayoutEntry.asSlice(structs, index.toLong())
    WGPUBindGroupLayoutEntry.binding(struct, entry.binding)
    WGPUBindGroupLayoutEntry
      .visibility(struct, SHADER_STAGE_VERTEX or SHADER_STAGE_FRAGMENT)
    when (entry.type) {
      BindingType.Texture -> {
        val texture = WGPUBindGroupLayoutEntry.texture(struct)
        WGPUTextureBindingLayout
          .sampleType(texture, WGPUTextureSampleType_Float())
        WGPUTextureBindingLayout
          .viewDimension(texture, WGPUTextureViewDimension_2D())
      }

      BindingType.Sampler -> {
        WGPUSamplerBindingLayout.type(
          WGPUBindGroupLayoutEntry.sampler(struct),
          WGPUSamplerBindingType_Filtering(),
        )
      }

      BindingType.ReadOnlyStorage -> {
        WGPUBufferBindingLayout.type(
          WGPUBindGroupLayoutEntry.buffer(struct),
          WGPUBufferBindingType_ReadOnlyStorage(),
        )
      }
    }
  }
  return structs
}

private fun pipelineLayoutDescriptor(
  arena: Arena,
  bindGroupLayouts: List<BindGroupLayout>,
  label: String?,
): MemorySegment {
  val descriptor = WGPUPipelineLayoutDescriptor.allocate(arena)
  if (label != null) {
    WGPUPipelineLayoutDescriptor.label(descriptor).setStringView(arena, label)
  }
  val handles = arena.allocate(ADDRESS, bindGroupLayouts.size.toLong())
  bindGroupLayouts.forEachIndexed { index, layout ->
    handles.setAtIndex(ADDRESS, index.toLong(), layout.handle)
  }
  WGPUPipelineLayoutDescriptor
    .bindGroupLayoutCount(descriptor, bindGroupLayouts.size.toLong())
  WGPUPipelineLayoutDescriptor.bindGroupLayouts(descriptor, handles)
  return descriptor
}

private fun renderPipelineDescriptor(
  arena: Arena,
  layout: PipelineLayout,
  vertex: VertexState,
  fragment: FragmentState,
  label: String?,
): MemorySegment {
  val descriptor = WGPURenderPipelineDescriptor.allocate(arena)
  if (label != null) {
    WGPURenderPipelineDescriptor.label(descriptor).setStringView(arena, label)
  }
  WGPURenderPipelineDescriptor.layout(descriptor, layout.handle)
  WGPURenderPipelineDescriptor.vertex(descriptor).setVertexState(arena, vertex)
  WGPUPrimitiveState.topology(
    WGPURenderPipelineDescriptor.primitive(descriptor),
    WGPUPrimitiveTopology_TriangleList(),
  )
  WGPURenderPipelineDescriptor
    .multisample(descriptor)
    .setSingleSample()
  WGPURenderPipelineDescriptor
    .fragment(descriptor, fragmentState(arena, fragment))
  return descriptor
}

private fun MemorySegment.setVertexState(
  arena: Arena,
  vertex: VertexState,
) {
  WGPUVertexState.module(this, vertex.module.handle)
  val entryPoint = WGPUVertexState.entryPoint(this)
  when (val point = vertex.entryPoint) {
    VertexEntryPoint.Auto -> entryPoint.setNullStringView()
    is VertexEntryPoint.Named -> entryPoint.setStringView(arena, point.name)
  }
}

private fun MemorySegment.setSingleSample() {
  WGPUMultisampleState.count(this, 1)
  // Every bit set enables all samples
  WGPUMultisampleState.mask(this, -1)
}

private fun fragmentState(
  arena: Arena,
  fragment: FragmentState,
): MemorySegment {
  val state = WGPUFragmentState.allocate(arena)
  WGPUFragmentState.module(state, fragment.module.handle)
  val entryPoint = WGPUFragmentState.entryPoint(state)
  when (val point = fragment.entryPoint) {
    FragmentEntryPoint.Auto -> entryPoint.setNullStringView()
    is FragmentEntryPoint.Named -> entryPoint.setStringView(arena, point.name)
  }
  WGPUFragmentState.targetCount(
    state,
    fragment.targets.size
      .toLong(),
  )
  WGPUFragmentState.targets(state, colorTargetStates(arena, fragment.targets))
  return state
}

private fun colorTargetStates(
  arena: Arena,
  targets: List<ColorTargetState>,
): MemorySegment {
  val states =
    WGPUColorTargetState.allocateArray(targets.size.toLong(), arena)
  targets.forEachIndexed { index, target ->
    val state = WGPUColorTargetState.asSlice(states, index.toLong())
    WGPUColorTargetState.format(state, target.format.native)
    WGPUColorTargetState.writeMask(state, COLOR_WRITE_MASK_ALL)
  }
  return states
}

private fun renderBundleEncoderDescriptor(
  arena: Arena,
  colorFormats: List<TextureFormat>,
  label: String?,
): MemorySegment {
  val descriptor = WGPURenderBundleEncoderDescriptor.allocate(arena)
  if (label != null) {
    WGPURenderBundleEncoderDescriptor
      .label(descriptor)
      .setStringView(arena, label)
  }
  val formats = arena.allocate(JAVA_INT, colorFormats.size.toLong())
  colorFormats.forEachIndexed { index, format ->
    formats.setAtIndex(JAVA_INT, index.toLong(), format.native)
  }
  WGPURenderBundleEncoderDescriptor
    .colorFormatCount(descriptor, colorFormats.size.toLong())
  WGPURenderBundleEncoderDescriptor.colorFormats(descriptor, formats)
  WGPURenderBundleEncoderDescriptor.sampleCount(descriptor, 1)
  return descriptor
}

private fun bufferDescriptor(
  arena: Arena,
  size: Int,
  usage: Set<BufferUsage>,
  label: String?,
): MemorySegment {
  val descriptor = WGPUBufferDescriptor.allocate(arena)
  if (label != null) {
    WGPUBufferDescriptor.label(descriptor).setStringView(arena, label)
  }
  WGPUBufferDescriptor.usage(
    descriptor,
    usage.fold(0L) { flags, each -> flags or each.native },
  )
  WGPUBufferDescriptor.size(descriptor, size.toLong())
  return descriptor
}

private fun textureDescriptor(
  arena: Arena,
  size: Pair<Int, Int>,
  format: TextureFormat,
  label: String?,
): MemorySegment {
  val [width, height] = size
  val descriptor = WGPUTextureDescriptor.allocate(arena)
  if (label != null) {
    WGPUTextureDescriptor.label(descriptor).setStringView(arena, label)
  }
  WGPUTextureDescriptor.usage(
    descriptor,
    TEXTURE_USAGE_COPY_DST or TEXTURE_USAGE_TEXTURE_BINDING,
  )
  WGPUTextureDescriptor.dimension(descriptor, WGPUTextureDimension_2D())
  val extent = WGPUTextureDescriptor.size(descriptor)
  WGPUExtent3D.width(extent, width)
  WGPUExtent3D.height(extent, height)
  WGPUExtent3D.depthOrArrayLayers(extent, 1)
  WGPUTextureDescriptor.format(descriptor, format.native)
  WGPUTextureDescriptor.mipLevelCount(descriptor, 1)
  WGPUTextureDescriptor.sampleCount(descriptor, 1)
  return descriptor
}

private fun samplerDescriptor(
  arena: Arena,
  magFilter: FilterMode,
  minFilter: FilterMode,
  label: String?,
): MemorySegment {
  val descriptor = WGPUSamplerDescriptor.allocate(arena)
  if (label != null) {
    WGPUSamplerDescriptor.label(descriptor).setStringView(arena, label)
  }
  WGPUSamplerDescriptor.addressModeU(descriptor, WGPUAddressMode_ClampToEdge())
  WGPUSamplerDescriptor.addressModeV(descriptor, WGPUAddressMode_ClampToEdge())
  WGPUSamplerDescriptor.addressModeW(descriptor, WGPUAddressMode_ClampToEdge())
  WGPUSamplerDescriptor.magFilter(descriptor, magFilter.native)
  WGPUSamplerDescriptor.minFilter(descriptor, minFilter.native)
  WGPUSamplerDescriptor
    .mipmapFilter(descriptor, WGPUMipmapFilterMode_Nearest())
  // The defaults of WGPU_SAMPLER_DESCRIPTOR_INIT, as allocate zeroes them
  WGPUSamplerDescriptor.lodMaxClamp(descriptor, LOD_MAX_CLAMP)
  WGPUSamplerDescriptor.maxAnisotropy(descriptor, 1)
  return descriptor
}

private fun bindGroupDescriptor(
  arena: Arena,
  layout: BindGroupLayout,
  entries: List<BindGroupEntry>,
  label: String?,
): MemorySegment {
  val descriptor = WGPUBindGroupDescriptor.allocate(arena)
  if (label != null) {
    WGPUBindGroupDescriptor.label(descriptor).setStringView(arena, label)
  }
  WGPUBindGroupDescriptor.layout(descriptor, layout.handle)
  WGPUBindGroupDescriptor.entryCount(descriptor, entries.size.toLong())
  WGPUBindGroupDescriptor.entries(descriptor, bindGroupEntries(arena, entries))
  return descriptor
}

private fun bindGroupEntries(
  arena: Arena,
  entries: List<BindGroupEntry>,
): MemorySegment {
  val structs =
    WGPUBindGroupEntry.allocateArray(entries.size.toLong(), arena)
  entries.forEachIndexed { index, entry ->
    val struct = WGPUBindGroupEntry.asSlice(structs, index.toLong())
    WGPUBindGroupEntry.binding(struct, entry.binding)
    when (val resource = entry.resource) {
      is Buffer -> {
        struct.setBuffer(resource, WGPU_WHOLE_SIZE())
      }

      is BufferBinding -> {
        struct.setBuffer(resource.buffer, resource.size.toLong())
      }

      is Sampler -> {
        WGPUBindGroupEntry.sampler(struct, resource.handle)
      }

      is TextureView -> {
        WGPUBindGroupEntry.textureView(struct, resource.handle)
      }
    }
  }
  return structs
}

private fun MemorySegment.setBuffer(
  buffer: Buffer,
  size: Long,
) {
  WGPUBindGroupEntry.buffer(this, buffer.handle)
  WGPUBindGroupEntry.size(this, size)
}

// jextract skips it, as webgpu.h declares it as static const
private const val COLOR_WRITE_MASK_ALL = 0xFL

// jextract skips them, as webgpu.h declares them as static const
private const val SHADER_STAGE_VERTEX = 0x1L
private const val SHADER_STAGE_FRAGMENT = 0x2L

private val BufferUsage.native: Long
  get() =
    when (this) {
      BufferUsage.CopyDst -> BUFFER_USAGE_COPY_DST
      BufferUsage.Uniform -> BUFFER_USAGE_UNIFORM
      BufferUsage.Storage -> BUFFER_USAGE_STORAGE
      BufferUsage.Indirect -> BUFFER_USAGE_INDIRECT
    }

// jextract skips them, as webgpu.h declares them as static const
private const val BUFFER_USAGE_COPY_DST = 0x8L
private const val BUFFER_USAGE_UNIFORM = 0x40L
private const val BUFFER_USAGE_STORAGE = 0x80L
private const val BUFFER_USAGE_INDIRECT = 0x100L

// jextract skips them, as webgpu.h declares them as static const
private const val TEXTURE_USAGE_COPY_DST = 0x2L
private const val TEXTURE_USAGE_TEXTURE_BINDING = 0x4L

private const val LOD_MAX_CLAMP = 32f

// endregion
