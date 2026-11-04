# Kotlin

## Layout

The API of a file is the declarations that other files use.

Keep the API at the top of each file so it reads without IDE tools.

* An API declaration has a one-line body, or calls one function and only
  passes it parameters, properties or getter-like calls. Lambdas it passes
  follow the same rule.
  An inline function that delegates does so to a `@PublishedApi internal
  inline` one.
* Fields stay at the top of a class regardless of visibility.
* Put the other declarations below them, inside a region, in this order:
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
