package net.tobysullivan.shorturl.test

import net.tobysullivan.shorturl._
import org.scalatest.FlatSpec
import scala.collection.mutable.ArraySeq
import net.tobysullivan.shorturl.AvailableHashManager

class AvailableHashManagerSpec extends FlatSpec {
  def fixture =
    new {
      val hashManager = new AvailableHashManager()(Configuration.HASH_STORE)
    }
  
  "getNext" should "produce a unique, larger value every time" in {
    val numRuns = 1000
    
    var lastVal = 0;
    for(i <- 1 to numRuns) {
      
      val hash = fixture.hashManager.getNext
      
      assert(hash > lastVal)
      
      lastVal = hash
    }
  }
  
  it should "not trip up with many threads executing" in {
    val numThreads = 100;
    val active = new ArraySeq[Thread](numThreads)
    
    for(i <- 0 to (numThreads - 1)) {
      
      val thread = new Thread(new FreeHashGetter(fixture.hashManager), "freehash-"+i)
      thread.start
      active.update(i, thread)
    }
    
    active.foreach(thread => thread.join())
  }
}

class FreeHashGetter(hashManager: AvailableHashManager) extends Runnable {
  def run {
    val hash = hashManager.getNext
    
    assert(hash > 0)
  }
} 