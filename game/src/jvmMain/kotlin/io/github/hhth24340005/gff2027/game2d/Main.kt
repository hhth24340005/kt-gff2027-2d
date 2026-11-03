package io.github.hhth24340005.gff2027.game2d

import io.github.hhth24340005.gff2027.engine2d.Canvas
import io.github.hhth24340005.gff2027.engine2d.Image
import io.github.hhth24340005.gff2027.webgpu.resourceCoroutineScope
import io.github.hhth24340005.gff2027.window.CloseRequests
import io.github.hhth24340005.gff2027.window.Window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.imageio.ImageIO

public fun main(): Unit =
  runBlocking {
    val window =
      Window
        .createOnCurrentDispatcher(title = "Sprite", size = WIDTH to HEIGHT)
    val closeRequests = CloseRequests()
    window.use(closeRequests) {
      val game =
        launch(Dispatchers.Default) {
          resourceCoroutineScope {
            val canvas = Canvas(window)
            drawSprites(canvas, WIDTH to HEIGHT, loadImage(SPRITE_PATH))
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
  val size = image.width to image.height
  val argb =
    image.getRGB(0, 0, image.width, image.height, null, 0, image.width)
  return Image(size, argb)
}

private const val SPRITE_PATH = "circle.png"

private const val WIDTH = 640
private const val HEIGHT = 480

// endregion
