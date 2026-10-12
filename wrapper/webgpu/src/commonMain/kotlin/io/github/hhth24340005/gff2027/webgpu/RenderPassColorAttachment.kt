package io.github.hhth24340005.gff2027.webgpu

/** A color target of a render pass, drawn through [view]. */
public class RenderPassColorAttachment(
  public val view: TextureView,
  public val load: LoadOp,
  public val store: StoreOp = StoreOp.Store,
)

/** What a render pass does with an attachment before drawing. */
public sealed interface LoadOp {
  /** Keeps the current contents. */
  public data object Load : LoadOp

  /** Fills the attachment with [color]. */
  public class Clear(public val color: Color) : LoadOp
}

/** What a render pass does with an attachment after drawing. */
public enum class StoreOp {
  /** Keeps the result. */
  Store,

  /** Throws the result away. */
  Discard,
}
