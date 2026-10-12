# Kotlin

## Layout

Keep the public API at the top of each file so it reads without IDE tools.

* Public declarations call one function and only pass it parameters,
  properties or getter-like calls. Lambdas they pass follow the same rule.
  Inline functions delegate to `@PublishedApi internal inline` ones.
* Fields stay at the top of a class regardless of visibility.
* Put non-public declarations below them, inside a region, in this order:
  1. `@PublishedApi internal`
  2. `internal`
  3. `private`

  ```kotlin
  public inline fun <R> measure(label: String, block: () -> R): R =
    measureImpl(label, block)

  // region internal

  @PublishedApi
  internal inline fun <R> measureImpl(label: String, block: () -> R): R { ... }

  internal fun report(label: String, elapsed: Long) { ... }

  private fun now(): Long = ...

  // endregion
  ```
