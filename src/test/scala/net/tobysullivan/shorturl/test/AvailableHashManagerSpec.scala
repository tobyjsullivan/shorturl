package net.tobysullivan.shorturl.test

import net.tobysullivan.shorturl._
import org.scalatest.FlatSpec
import scala.collection.mutable.ArraySeq

class AvailableHashManagerSpec extends FlatSpec {
  "getNext" should "produce a unique, larger value every time" in {
    val numRuns = 10000
    
    var lastVal = 0;
    for(i <- 1 to numRuns) {
      val hash = AvailableHashManager.getNext
      
      assert(hash > lastVal)
      
      lastVal = hash
    }
  }
  
  it should "not trip up with many threads executing" in {
    val numThreads = 2500;
    val active = new ArraySeq[Thread](numThreads)
    
    for(i <- 0 to (numThreads - 1)) {
      
      val thread = new Thread(new FreeHashGetter, "freehash-"+i)
      thread.start
      active.update(i, thread)
    }
    
    active.foreach(thread => thread.join())
  }
}

class FreeHashGetter extends Runnable {
  def run {
    val hash = AvailableHashManager.getNext
    
    assert(hash > 0)
  }
} 