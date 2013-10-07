package net.tobysullivan.shorturl

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import akka.agent.Agent

/**
 * This object manages the next available free hash
 */
class AvailableHashManager(implicit hashStore: HashStore) {
  
  /**
   * This method returns the next available hash as an Int in a thread-safe manner.
   */
//  private val cursor = Agent(HashStore.findNextAvailableHash()(hashStore))
  def getNext(): Int = {
//    val future = cursor alter (_ + 1)
//    Await.result(future, 30 second)
    
    HashStore.findNextAvailableHash()(hashStore)
  }
}