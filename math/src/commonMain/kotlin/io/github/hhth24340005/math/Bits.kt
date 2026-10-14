package io.github.hhth24340005.math

internal fun Float.toFiniteBitsOrThrow(): Int {
  require(isFinite()) { "Value must be finite, but got: $this" }
  // Adding zero turns -0 into 0, so equal values share their bits
  return (this + 0.0f).toRawBits()
}

internal fun Float.toFiniteBitsOrOverflow(): Int {
  if (!isFinite()) {
    throw ArithmeticException("Result is beyond the Float range: $this")
  }
  // Adding zero turns -0 into 0, so equal values share their bits
  return (this + 0.0f).toRawBits()
}
