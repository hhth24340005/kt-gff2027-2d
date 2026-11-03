@file:Suppress("MethodOverloading")

package io.github.hhth24340005.gff2027.engine2d

import io.github.hhth24340005.gff2027.webgpu.BindingType
import kotlin.reflect.KClass

/**
 * WGSL whose only `@vertex` and `@fragment` functions draw [vertices] as a
 * triangle list. Chain `bind` to declare what it reads, which it finds at
 * these bindings:
 *
 * * `@group(0) @binding(k)`: the resource bound k-th, from 0. A [WgslStruct]
 *   is `var<storage, read>`, a [WgslArray] is `var<storage, read>` of
 *   `array<T>`, and an [Image] is `texture_2d<f32>`.
 * * `@group(1) @binding(k)`: the `sampler` of the [Image] bound k-th.
 * * `@group(2) @binding(0)`: the instances as `var<storage, read>` of
 *   `array<Instance>`, indexed by `instance_index`.
 */
public fun <Vertex : WgslStruct> shader(
  wgsl: String,
  vertices: Vertices<Vertex>,
): Shader0<Vertex, Nothing> =
  newShader(wgsl, vertices, instances = null)

/**
 * Same as the other `shader`, but draws once for each [instances] in the
 * list that a [Layer] takes, instead of once.
 */
public fun <Vertex : WgslStruct, Instance : WgslStruct> shader(
  wgsl: String,
  vertices: Vertices<Vertex>,
  @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
  instances: KClass<Instance>,
): Shader0<Vertex, Instance> =
  newShader(wgsl, vertices, instances)

/** The vertices a shader draws, which it tells apart by `vertex_index`. */
public sealed interface Vertices<Vertex : WgslStruct>

/** [count] vertices without data. */
public class VertexCount(public val count: Int) : Vertices<Nothing>

/**
 * A shader of a [Pipeline]. `Vertex` and `Instance` are the structs it
 * reads per vertex and per instance, or `Nothing` for none, and the rest
 * are the resources bound in order.
 */
public sealed class Shader protected constructor(
  internal val wgsl: String,
  internal val vertexCount: Int,
  internal val hasInstances: Boolean,
  internal val bindingTypes: List<BindingType>,
) {
  protected constructor(
    previous: Shader,
    resource: KClass<out ShaderResource>,
  ) : this(
    previous.wgsl,
    previous.vertexCount,
    previous.hasInstances,
    previous.bindingTypes + bindingTypeOf(resource),
  )
}

public class Shader0<
  Vertex : WgslStruct,
  Instance : WgslStruct,
>
  internal constructor(
    wgsl: String,
    vertexCount: Int,
    hasInstances: Boolean,
  ) : Shader(wgsl, vertexCount, hasInstances, []) {
    public inline fun <reified A : ShaderResource> bind():
      Shader1<Vertex, Instance, A> =
      Shader1(this, A::class)
  }

public class Shader1<
  Vertex : WgslStruct,
  Instance : WgslStruct,
  A : ShaderResource,
>
  @PublishedApi
  internal constructor(
    previous: Shader,
    resource: KClass<out ShaderResource>,
  ) : Shader(previous, resource) {
    public inline fun <reified B : ShaderResource> bind():
      Shader2<Vertex, Instance, A, B> =
      Shader2(this, B::class)
  }

public class Shader2<
  Vertex : WgslStruct,
  Instance : WgslStruct,
  A : ShaderResource,
  B : ShaderResource,
>
  @PublishedApi
  internal constructor(
    previous: Shader,
    resource: KClass<out ShaderResource>,
  ) : Shader(previous, resource) {
    public inline fun <reified C : ShaderResource> bind():
      Shader3<Vertex, Instance, A, B, C> =
      Shader3(this, C::class)
  }

public class Shader3<
  Vertex : WgslStruct,
  Instance : WgslStruct,
  A : ShaderResource,
  B : ShaderResource,
  C : ShaderResource,
>
  @PublishedApi
  internal constructor(
    previous: Shader,
    resource: KClass<out ShaderResource>,
  ) : Shader(previous, resource) {
    public inline fun <reified D : ShaderResource> bind():
      Shader4<Vertex, Instance, A, B, C, D> =
      Shader4(this, D::class)
  }

public class Shader4<
  Vertex : WgslStruct,
  Instance : WgslStruct,
  A : ShaderResource,
  B : ShaderResource,
  C : ShaderResource,
  D : ShaderResource,
>
  @PublishedApi
  internal constructor(
    previous: Shader,
    resource: KClass<out ShaderResource>,
  ) : Shader(previous, resource)

// region internal

private fun bindingTypeOf(
  type: KClass<out ShaderResource>,
): BindingType =
  if (type == Image::class) Texture else ReadOnlyStorage

private fun <Vertex : WgslStruct, Instance : WgslStruct> newShader(
  wgsl: String,
  vertices: Vertices<Vertex>,
  instances: KClass<Instance>?,
): Shader0<Vertex, Instance> {
  val count =
    when (vertices) {
      is VertexCount -> vertices.count
    }
  val hasInstances = instances != null
  return Shader0(wgsl, count, hasInstances)
}

// endregion
