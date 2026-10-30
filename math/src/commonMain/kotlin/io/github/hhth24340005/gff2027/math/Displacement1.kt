package io.github.hhth24340005.gff2027.math

// Guides those who guess Displacement1 from Displacement2
@Deprecated(
  "Use HorizontalDistance or VerticalDistance, or Distance for a non-negative magnitude",
  ReplaceWith("HorizontalDistance"),
  DeprecationLevel.ERROR,
)
public typealias Displacement1 = HorizontalDistance
