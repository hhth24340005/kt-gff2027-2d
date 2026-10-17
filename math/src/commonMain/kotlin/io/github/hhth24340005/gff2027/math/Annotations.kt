package io.github.hhth24340005.gff2027.math

/**
 * Marks the origin of the plane.
 *
 * Positions usually come from other positions, so only code that sets up
 * a coordinate system, such as a scene, needs the origin.
 */
@RequiresOptIn(
  "The origin is only for setting up a coordinate system",
  RequiresOptIn.Level.ERROR,
)
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY)
public annotation class OriginApi
