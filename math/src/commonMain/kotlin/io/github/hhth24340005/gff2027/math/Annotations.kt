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

/**
 * Marks the raw numbers inside the quantities, which lose the meaning their
 * types carry.
 *
 * Only code that hands them outside Kotlin, such as to the GPU, needs them.
 */
@RequiresOptIn(
  "Raw values are only for handing quantities outside Kotlin",
  RequiresOptIn.Level.ERROR,
)
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY)
public annotation class RawValueApi
