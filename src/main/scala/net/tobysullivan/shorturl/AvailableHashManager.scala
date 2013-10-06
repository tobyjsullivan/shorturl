package net.tobysullivan.shorturl

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import akka.agent.Agent

/**
 * This object manages the next available free hash
 */
object AvailableHashManager {
  /**
   * This method returns the next available hash as an Int in a thread-safe manner.
   */  
  private val cursor = Agent(HashStore.findNextAvailableHash)
  def getNext(): Int = {
    synchronized {
      val future = cursor alter (_ + 1)
      Await.result(future, 1 second)
    }
  }
}