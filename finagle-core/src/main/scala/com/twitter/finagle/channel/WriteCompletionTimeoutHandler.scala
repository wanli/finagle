package com.twitter.finagle.channel

/**
 * A simple handler that times out a write if it fails to complete
 * within the given time. This can be used to ensure that clients
 * complete reception within a certain time, preventing a resource DoS
 * on a server.
 */

import org.jboss.netty.channel.{
  SimpleChannelDownstreamHandler, Channels,
  ChannelHandlerContext, MessageEvent}

import com.twitter.util.{Time, Duration, Timer}
import com.twitter.conversions.time._

import com.twitter.finagle.util.Conversions._
import com.twitter.finagle.WriteTimedOutException

class WriteCompletionTimeoutHandler(timer: Timer, timeout: Duration)
  extends SimpleChannelDownstreamHandler
{
  override def writeRequested(ctx: ChannelHandlerContext, e: MessageEvent) {
    val task = timer.schedule(Time.now + timeout) {
      Channels.fireExceptionCaught(
        ctx.getChannel, new WriteTimedOutException)
    }
    e.getFuture onSuccessOrFailure { task.cancel() }
    super.writeRequested(ctx, e)
  }
}
