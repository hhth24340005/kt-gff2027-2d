package io.github.hhth24340005.gff2027.webgpu

// region internal

// The entries follow WGPUTextureFormat, which numbers them from 1 without gaps
internal val TextureFormat.native: Int
  get() = ordinal + 1

// Returns null for formats only wgpu-native defines
internal fun textureFormatOf(
  native: Int,
): TextureFormat? =
  TextureFormat.entries.getOrNull(native - 1)

// endregion
