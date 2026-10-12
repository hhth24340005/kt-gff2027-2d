package io.github.hhth24340005.gff2027.window

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Receives the requests to close a window passed to [Window.use].
 * Requests are conflated, so none is lost while nobody is receiving.
 */
public class CloseRequests private constructor(
  @PublishedApi internal val channel: Channel<Unit>,
) : ReceiveChannel<Unit> by channel {
  public constructor() : this(Channel(Channel.CONFLATED))
}
