package net.tobysullivan.shorturl.test

import org.scalatest.FlatSpec
import net.tobysullivan.shorturl.StatsStore

class StatsStoreSpec extends FlatSpec {
  def fixture =
    new {
      val statsStore = Configuration.STATS_STORE
    }
  "incrementHashLookupCount" should "not fail when provided valid data" in {
    val hash = 283 // arbitrary
    
    fixture.statsStore.incrementHashLookupCount(hash)
  }
  
  "getHashLookupCount" should "return zero for a new hash" in {
    val hash = 20000000 + RandomHelper.genInt(10000)
    
    val count = fixture.statsStore.getHashLookupCount(hash)
    
    assert(count == 0)
  }
  
  "getHashLookupCount" should "return the correct number of increments for a hash" in {
    val hash = 10000000 + RandomHelper.genInt(10000)
    val numIncrements = 395
    
    for(i <- 1 to numIncrements) {
      fixture.statsStore.incrementHashLookupCount(hash)
    }
    
    val count = fixture.statsStore.getHashLookupCount(hash)
    
    assert(count == numIncrements)
  }
}