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
    
    StatsStore.incrementHashLookupCount(hash)(fixture.statsStore)
  }
  
  "getHashLookupCount" should "return zero for a new hash" in {
    val hash = 20000000 + RandomHelper.genInt(10000)
    
    val count = StatsStore.getHashLookupCount(hash)(fixture.statsStore)
    
    assert(count == 0)
  }
  
  "getHashLookupCount" should "return the correct number of increments for a hash" in {
    val hash = 10000000 + RandomHelper.genInt(10000)
    val numIncrements = 395
    
    for(i <- 1 to numIncrements) {
      StatsStore.incrementHashLookupCount(hash)(fixture.statsStore)
    }
    
    val count = StatsStore.getHashLookupCount(hash)(fixture.statsStore)
    
    assert(count == numIncrements)
  }
}