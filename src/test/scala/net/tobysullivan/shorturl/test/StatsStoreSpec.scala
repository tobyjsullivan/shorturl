package net.tobysullivan.shorturl.test

import org.scalatest.FlatSpec
import net.tobysullivan.shorturl.StatsStore

class StatsStoreSpec extends FlatSpec {
  "incrementHashLookupCount" should "not fail when provided valid data" in {
    val hash = 283 // arbitrary
    
    StatsStore.incrementHashLookupCount(hash)
  }
  
  "getHashLookupCount" should "return zero for a new hash" in {
    val hash = RandomHelper.genInt(10000)
    
    val count = StatsStore.getHashLookupCount(hash)
    
    assert(count == 0)
  }
  
  "getHashLookupCount" should "return the correct number of increments for a hash" in {
    val hash = RandomHelper.genInt(10000)
    val numIncrements = 395
    
    for(i <- 1 to numIncrements) {
      StatsStore.incrementHashLookupCount(hash)
    }
    
    val count = StatsStore.getHashLookupCount(hash)
    
    assert(count == numIncrements)
  }
}