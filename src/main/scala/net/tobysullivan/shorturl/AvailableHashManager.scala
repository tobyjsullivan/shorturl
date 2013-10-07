package net.tobysullivan.shorturl

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import akka.agent.Agent

/**
 * This object manages the next available free hash
 */
class AvailableHashManager(hashStore: HashStore) {
  
  /**
   * This method returns the next available hash as an Int in a thread-safe manner.
   */
  def getNext(): Int = {
    hashStore.findNextAvailableHash()
  }
}