@file:Suppress("MethodOverloading")

package io.github.hhth24340005.gff2027.engine2d

import kotlinx.coroutines.flow.StateFlow

/**
 * What a canvas draws with one [Pipeline]: the pipeline and the resources it
 * reads, each the current value of a [StateFlow]. A layer is immutable, so
 * create another to change what it draws.
 */
public class Layer<Format : ColorFormat> internal constructor(
  internal val pipeline: Pipeline<Format, Shader>,
  internal val resources: List<StateFlow<ShaderResource>>,
  internal val items: StateFlow<List<WgslStruct>>?,
)

// region Shader0

/** Draws [pipeline] once. */
public fun <Format : ColorFormat> Layer(
  pipeline: Pipeline<Format, Shader0<*, Nothing>>,
): Layer<Format> =
  newLayer(pipeline, [], items = null)

/** Draws [pipeline] once for each of [items]. */
public fun <Format : ColorFormat, Instance : WgslStruct> Layer(
  pipeline: Pipeline<Format, Shader0<*, Instance>>,
  @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
  items: StateFlow<List<Instance>>,
): Layer<Format> =
  newLayer(pipeline, [], items)

// endregion

// region Shader1

/** Draws [pipeline] once with [a]. */
public fun <Format : ColorFormat, A : ShaderResource> Layer(
  pipeline: Pipeline<Format, Shader1<*, Nothing, A>>,
  a: StateFlow<A>,
): Layer<Format> =
  newLayer(pipeline, [a], items = null)

/** Draws [pipeline] once for each of [items] with [a]. */
public fun <
  Format : ColorFormat,
  Instance : WgslStruct,
  A : ShaderResource,
> Layer(
  pipeline: Pipeline<Format, Shader1<*, Instance, A>>,
  a: StateFlow<A>,
  @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
  items: StateFlow<List<Instance>>,
): Layer<Format> =
  newLayer(pipeline, [a], items)

// endregion

// region Shader2

/** Draws [pipeline] once with [a], [b]. */
public fun <
  Format : ColorFormat,
  A : ShaderResource,
  B : ShaderResource,
> Layer(
  pipeline: Pipeline<Format, Shader2<*, Nothing, A, B>>,
  a: StateFlow<A>,
  b: StateFlow<B>,
): Layer<Format> =
  newLayer(pipeline, [a, b], items = null)

/** Draws [pipeline] once for each of [items] with [a], [b]. */
public fun <
  Format : ColorFormat,
  Instance : WgslStruct,
  A : ShaderResource,
  B : ShaderResource,
> Layer(
  pipeline: Pipeline<Format, Shader2<*, Instance, A, B>>,
  a: StateFlow<A>,
  b: StateFlow<B>,
  @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
  items: StateFlow<List<Instance>>,
): Layer<Format> =
  newLayer(pipeline, [a, b], items)

// endregion

// region Shader3

/** Draws [pipeline] once with [a], [b], [c]. */
public fun <
  Format : ColorFormat,
  A : ShaderResource,
  B : ShaderResource,
  C : ShaderResource,
> Layer(
  pipeline: Pipeline<Format, Shader3<*, Nothing, A, B, C>>,
  a: StateFlow<A>,
  b: StateFlow<B>,
  c: StateFlow<C>,
): Layer<Format> =
  newLayer(pipeline, [a, b, c], items = null)

/** Draws [pipeline] once for each of [items] with [a], [b], [c]. */
public fun <
  Format : ColorFormat,
  Instance : WgslStruct,
  A : ShaderResource,
  B : ShaderResource,
  C : ShaderResource,
> Layer(
  pipeline: Pipeline<Format, Shader3<*, Instance, A, B, C>>,
  a: StateFlow<A>,
  b: StateFlow<B>,
  c: StateFlow<C>,
  @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
  items: StateFlow<List<Instance>>,
): Layer<Format> =
  newLayer(pipeline, [a, b, c], items)

// endregion

// region Shader4

/** Draws [pipeline] once with [a], [b], [c], [d]. */
public fun <
  Format : ColorFormat,
  A : ShaderResource,
  B : ShaderResource,
  C : ShaderResource,
  D : ShaderResource,
> Layer(
  pipeline: Pipeline<Format, Shader4<*, Nothing, A, B, C, D>>,
  a: StateFlow<A>,
  b: StateFlow<B>,
  c: StateFlow<C>,
  d: StateFlow<D>,
): Layer<Format> =
  newLayer(pipeline, [a, b, c, d], items = null)

/** Draws [pipeline] once for each of [items] with [a], [b], [c], [d]. */
public fun <
  Format : ColorFormat,
  Instance : WgslStruct,
  A : ShaderResource,
  B : ShaderResource,
  C : ShaderResource,
  D : ShaderResource,
> Layer(
  pipeline: Pipeline<Format, Shader4<*, Instance, A, B, C, D>>,
  a: StateFlow<A>,
  b: StateFlow<B>,
  c: StateFlow<C>,
  d: StateFlow<D>,
  @Suppress("UNUSED_PARAMETER") vararg namedOnly: NamedOnly,
  items: StateFlow<List<Instance>>,
): Layer<Format> =
  newLayer(pipeline, [a, b, c, d], items)

// endregion

// region internal

internal val Layer<*>.flows: List<StateFlow<Any>>
  get() = resources + listOfNotNull(items)

private fun <Format : ColorFormat> newLayer(
  pipeline: Pipeline<Format, Shader>,
  resources: List<StateFlow<ShaderResource>>,
  items: StateFlow<List<WgslStruct>>?,
): Layer<Format> =
  Layer(pipeline, resources, items)

// endregion
