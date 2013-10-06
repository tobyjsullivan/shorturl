package net.tobysullivan.shorturl

import net.tobysullivan.shorturl.config.Configuration

object StatsStore {
  val implementation: StatsStore = Configuration.STATS_STORE
  
  def incrementHashLookupCount(hash: Int) {
    implementation.incrementHashLookupCount(hash)
  }
  
  def getHashLookupCount(hash: Int): Int = {
    implementation.getHashLookupCount(hash)
  }
}

trait StatsStore {
  def incrementHashLookupCount(hash: Int)
  
  def getHashLookupCount(hash: Int): Int
}