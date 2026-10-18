package io.github.hhth24340005.gff2027.game2d

import io.github.hhth24340005.gff2027.webgpu.Instance
import io.github.hhth24340005.gff2027.webgpu.createSurface
import io.github.hhth24340005.gff2027.webgpu.resourceCoroutineScope
import io.github.hhth24340005.gff2027.window.CloseRequests
import io.github.hhth24340005.gff2027.window.Window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.imageio.ImageIO
import kotlin.coroutines.ContinuationInterceptor

public fun main(): Unit =
  runBlocking {
    val window =
      Window
        .createOnCurrentDispatcher(title = "Sprite", size = WIDTH to HEIGHT)
    val closeRequests = CloseRequests()
    window.use(closeRequests) {
      val mainThread = coroutineContext[ContinuationInterceptor]!!
      val game =
        launch(Dispatchers.Default) {
          resourceCoroutineScope {
            val instance = Instance()
            drawSprite(
              instance,
              instance.createSurface(window),
              WIDTH to HEIGHT,
              mainThread,
              loadImage(SPRITE_PATH),
            )
          }
        }
      closeRequests.receive()
      game.cancelAndJoin()
    }
  }

// region internal

private fun loadImage(
  name: String,
): Image {
  val image =
    ClassLoader.getSystemResourceAsStream(name)?.use { ImageIO.read(it) }
      ?: error("No image resource $name")
  val argb =
    image.getRGB(0, 0, image.width, image.height, null, 0, image.width)
  return Image(image.width to image.height, argb)
}

private const val SPRITE_PATH = "circle.png"

private const val WIDTH = 640
private const val HEIGHT = 480

// endregion
